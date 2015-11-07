package concepture;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

/**
 * Contains the templates and gestures defined previously.
 * It matches a new gesture to the set of known gestures and uses various
 * functions from the Utils class in order to process the strokes.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
class Recognizer
{	
	static int NumPoints = 128;
	static double SquareSize = 250.0;
	static double HalfDiagonal = 0.5 * Math.sqrt(250.0 * 250.0 + 250.0 * 250.0);
	static double AngleRange = 45.0;
	static double AnglePrecision = 2.0;
	static double Phi = 0.5 * (-1.0 + Math.sqrt(5.0)); // Golden Ratio

	int bounds[] = { 0, 0, 0, 0 };
	private int template_limit = 50;
	private char names[] = new char[template_limit];
	
	static long elapsed = 0;

	private HashMap<String, Template> Templates = new HashMap<String, Template>();
	private Vector<Gesture> Gestures = new Vector<Gesture>();
	
	Recognizer()
	{	
		int start = (int)'A';
		int end = start + template_limit;
		int j = 0;
		for(int i=start; i<end; i++)
		{
			names[j++] = (char)i;						
		}		
	}

	String[] getGestureNames()
	{
		String[] gNames = new String[Gestures.size()];
		for(int i=0; i<Gestures.size(); i++)
		{
			gNames[i] = Gestures.elementAt(i).name;
		}
		return gNames;
	}
	
	Object[][] getGestureInfo()
	{
		Object[][] data = new Object[Gestures.size()][3];
		for(int i=0; i<Gestures.size(); i++)
		{
			Gesture g = Gestures.elementAt(i);
			data[i][0] = g.name;
			data[i][1] = g.color;
			data[i][2] = g.circular;
		}
		return data;
	}
	
	void deleteGesture(int which)
	{
		Gestures.remove(which);
	}
	
	void addGesture(Gesture gest)
	{
		Gestures.addElement(gest);
	}
	
	Vector<Point> makeGesture(String str, int w, int h, boolean circular)
	{
		Vector<Point> points = new Vector<Point>();
		Vector<Point> prev = new Vector<Point>();		
		double tweak = 0.0;
		
		for(int i=0; i<str.length(); i++)
		{
			String s = str.substring(i, i+1);
			Template tmpl = Templates.get(s);		
			
			if(circular)
				tweak = 6.28/(1.0+tmpl.getLabelLength()) - 6.28/(1.0+str.length());
		
			Vector<Point> addition = blendTemplates(prev, tmpl, w/2, h/2, -tweak);
			if(addition == null)
				return null;	// give up, the string is not sketchable
			points.addAll(addition);			
		}		
		Point pivot = prev.elementAt(0);
		prev = Utils.RotateAroundPoint(prev, -tweak, pivot);
		points.addAll(prev);	// the leftover segment
		return points;
	}
	
	private Vector<Point> blendTemplates(Vector<Point> prev, Template tmpl, 
			int w, int h, double tweak)
	{
		Vector<Point> points = new Vector<Point>();
		if(prev.size() == 0)	// if this is very first template
		{
			int halfWay = tmpl.getMidpoint();		
			if(tmpl.isDoubleStroke())
				halfWay = NumPoints-1;
			for(int i=0; i<=halfWay; i++)
			{
				Point p1 = tmpl.points.elementAt(i);
				Point p2 = new Point(p1.getX() + w, p1.getY() + h, false);		// or vice versa?
				points.add(p2);
			}					
			
			for(int i=halfWay+1; i<tmpl.points.size(); i++)
			{
				Point p1 = tmpl.points.elementAt(i);
				Point p2 = new Point(p1.getX() + w, p1.getY() + h, false);	
				prev.add(p2);
			}
		}
		else	// then blend the first half of this template with the previous second half
		{	
			int halfWay = tmpl.getMidpoint();
			if(tmpl.isDoubleStroke())
				halfWay = NumPoints-1;
			
			Vector<Point> curr = Utils.getBestOrientation(prev, tmpl.points, halfWay+1,
					-AngleRange, AngleRange, AnglePrecision, 0.3, tweak);
			
			if(curr == null)		// this means that the blending had a very bad score
				return null;					
			
			for(int i=0; i<=halfWay; i++)				
				points.add(new Point(curr.elementAt(i)));
			
			prev.clear();		// reset the previous
			for(int i=halfWay+1; i<curr.size(); i++)
			{
				Point p1 = curr.elementAt(i);
				Point p2 = new Point(p1);	
				prev.add(p2);
			}
		}
		return points;	
	}
	
	Answer processGesture(MultiStroke ms, Vector<Template> added)
	{
		String labels = "";		
		Vector<Point> segment1 = null;	
		Vector<Point> segment2 = null;	
				
		double totalScore = 0.0;
		for(int j=0; j<ms.strokes.size(); j++)
		{
			Stroke s = ms.strokes.elementAt(j);
			int[] corners = Utils.getMarkers(s.points);
			int num_corners = corners.length;						
					
			for(int i=0; i<num_corners-1; i++)
			{
				segment2 = Utils.getSegment(s.points, corners[i], corners[i+1]);										
				segment2 = Utils.resample(segment2, NumPoints);
				
				if(segment1 != null)
				{
					Vector<Point> segment = new Vector<Point>();
					segment.addAll(segment1);
					segment.addAll(segment2);		
					
					boolean doubleStroke = false;
					if(j>0 && i==0)	// it is a double stroke template
						doubleStroke = true;
					
					Answer ans = recognizeTemplate(segment, doubleStroke);						
					if(added != null && ans.score < 0.90)	// add a new template
					{
						MultiStroke newms = new MultiStroke();
						newms.addStroke(new Stroke(segment1));
						newms.addStroke(new Stroke(segment2));							
						labels += addTemplate(newms, added, doubleStroke);	
					}				
					else				
					{
						labels += ans.name;
						totalScore += ans.score;
					}
				}							
				segment1 = segment2;
			}
		}		
		if(added != null)
		{
			Enumeration<Template> e = added.elements();
			while(e.hasMoreElements())
			{
				Template tmpl = e.nextElement();
				tmpl.setLabelLength(labels.length());
			}
		}
		return (new Answer(labels, totalScore));
	}
	
	Answer recognizeGesture(MultiStroke ms, double cutoff, boolean circular)
	{
		Answer bestForward = null;
		Answer bestReverse = null;
		
		Answer forward = processGesture(ms, null);		
		System.out.println(forward.name);
		for(int j=0; j<Gestures.size(); j++)
		{
			Gesture gest = Gestures.elementAt(j);
			long start = System.currentTimeMillis();
			boolean matched = gest.isMember(forward.name);
			elapsed += System.currentTimeMillis() - start;
			if(matched)
			{				
				if(!gest.isCircular() || circular)
					return new Answer(gest.name, 1.0, forward.name, gest);
			}
		}
			
		Answer reverse = processGesture(ms.getMirror(), null);
		System.out.println(reverse.name);
		for(int j=0; j<Gestures.size(); j++)
		{
			Gesture gest = Gestures.elementAt(j);
			if(gest.isMember(reverse.name))
			{				
				if(!gest.isCircular() || circular)
					bestReverse = new Answer(gest.name, 1.0, reverse.name, gest);
			}
		}
		
		if(bestForward != null && bestReverse != null)
		{
			if(bestForward.score > bestReverse.score)
				return bestForward;
			return bestReverse;
		}
		else if(bestForward != null)
			return bestForward;
		
		return bestReverse;		// may or may not be null
	}
	
	private Answer recognizeTemplate(Vector<Point> points, boolean doubleStroke)
	{		
		points = Utils.RotateToZero(points);
		Utils.ScaleToUnit(points, SquareSize);
		Utils.TranslateToOrigin(points);			// does not change

		String t = null;
		double best = Double.MAX_VALUE;
		Iterator<String> iter = Templates.keySet().iterator();
		while(iter.hasNext())
		{
			Template tmpl = Templates.get(iter.next());
			if(tmpl.isActive() && tmpl.isDoubleStroke() == doubleStroke)
			{
				double d = Utils.DistanceAtBestAngle(points, tmpl.points, -AngleRange, AngleRange, AnglePrecision);
				if (d < best)
				{
					best = d;
					t = tmpl.name;
				}
			}
		}
		best = 1.0 - (best / HalfDiagonal);
		if(t == null)
			return new Answer("", 0.0);
		return new Answer(t, best);
	}

	String addTemplate(MultiStroke ms, Vector<Template> added, 
			boolean doubleStroke)
	{
		String s = new String(names, Templates.size(), 1);
		Template t = new Template(s, ms, doubleStroke);		
		added.add(t);
		Templates.put(s, t);
		return s;
	}
	
	void eraseTemplates(Vector<Template> added)
	{
		for(int i=0; i<added.size(); i++)
			added.elementAt(i).setActive(false);		
	}
	
	boolean writeSession(String filename)
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);			
			out.writeObject(Templates);
			out.writeObject(Gestures);
			out.close();			
			fos.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}	
		return true;
	}
	
	boolean readSession(String filename)
	{
		File f = new File(filename);
		if(!f.isFile())
			return false;
			
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			Templates = (HashMap<String, Template>)in.readObject();
			Gestures = (Vector<Gesture>)in.readObject();
			in.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(ClassNotFoundException e)
		{
			 e.printStackTrace();
			 return false;
		}
		return true;
	}
}
