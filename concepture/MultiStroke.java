package concepture;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Defines an ordered collection of strokes.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
class MultiStroke 
{
	Vector<Stroke> strokes;	
	
	MultiStroke()
	{
		strokes = new Vector<Stroke>();
	}
	
	MultiStroke(MultiStroke src)
	{		
		this.strokes = new Vector<Stroke>();
		Enumeration<Stroke> e = src.strokes.elements();
		while(e.hasMoreElements())			
		{
			this.addStroke(new Stroke(e.nextElement()));
		}
	}
	
	
	void addStroke(Stroke s)
	{
		strokes.addElement(s);
	}
	
	void clearStrokes()
	{
		strokes.removeAllElements();
	}
	
	void setColor(Color c)
	{		
		Enumeration<Stroke> e = strokes.elements();
		while(e.hasMoreElements())			
		{
			e.nextElement().setColor(c);
		}
	}
	
	Point getCentroid()
	{
		Enumeration<Stroke> e = strokes.elements();
		Vector<Point> points = new Vector<Point>(1);
		while(e.hasMoreElements())			
		{
			points.add( Utils.Centroid( e.nextElement().points ) );
		}
		return Utils.Centroid(points);		
	}
	
	MultiStroke getMirror()
	{
		MultiStroke ms = new MultiStroke();
		Enumeration<Stroke> e = strokes.elements();
		while(e.hasMoreElements())			
		{
			Stroke s = new Stroke(e.nextElement().getPoints(true));
			ms.addStroke(s);
		}
		return ms;
	}
	
	void rotateAroundPoint(Point p)
	{
		Enumeration<Stroke> e = strokes.elements();
		while(e.hasMoreElements())			
		{
			Stroke s = e.nextElement();
			s.points = Utils.RotateAroundPoint(s.points, 0.1, p);
		}		
	}
	
}
