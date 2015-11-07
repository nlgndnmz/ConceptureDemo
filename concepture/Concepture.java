package concepture;

import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Color;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.imageio.ImageIO;

/**
 * Features the Recognizer and also provides elements
 * to render the stroke drawn by the user in addition to the
 * annotations such as start of the stroke and detected corners.
 *
 * - Reads and writes (i.e. saves) the gesture and template files
 * by calling the recognizer.
 *
 * - Implements the actual mouse actions.
 *
 * - Recognizes a new sketch by first processing the strokes and
 * then calling the recognizer.
 *
 * @author Nilgun Donmez
 * @version 1.0
 */
public class Concepture
{
	Vector<Point> points;
	Vector<Point> queryPoints;
	
	MultiStroke mStroke;
	Vector<Drawing> drawings;
	
	BufferedImage backgroundImg;

	Recognizer recognizer;

	protected Gesture newGest = null;
	protected Answer result = null;

	boolean entering = true;
	protected boolean correcting = false;
	protected String prevStr = null;

	static final double clickDistance = 5.0;
	static final double connectDistance = 15.0;

	private Vector<Template> added = new Vector<Template>();
	
	public boolean clicked = false;

	public boolean isEntering()
	{
		return entering;
	}

	public void setEntering(boolean entering)
	{
		this.entering = entering;
	}

	public boolean isCorrecting()
	{
		return correcting;
	}

	public void setCorrecting(boolean correcting)
	{
		this.correcting = correcting;
	}
	
	public long getElapsedTime()
	{
		return Recognizer.elapsed;
	}

	private BufferedImage loadImage(String name, int count)
	{
		try
		{			
			return ImageIO.read(new File("./icons/" + name + "_" + count + ".png"));
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	public void loadBackgroundImage(String name)
	{		
		try
		{			
			backgroundImg = ImageIO.read(new File(name));
		}
		catch(IOException e)
		{
			backgroundImg = null;
		}
	}

	public Concepture()
	{
		recognizer = new Recognizer();
		mStroke = new MultiStroke();
		points = new Vector<Point>(1000);
		drawings = new Vector<Drawing>(1);
		entering = true;
		backgroundImg = null;
	}

	public void reInitialize()
	{
		mStroke = new MultiStroke();
		points = new Vector<Point>(1000);
		drawings = new Vector<Drawing>(1);
		entering = true;
		correcting = false;
		clicked = false;
		prevStr = null;
		result = null;
		newGest = null;						
	}

	public void render(Graphics g)
	{
		for(int j=0; j<mStroke.strokes.size(); j++)
		{
			Stroke s = mStroke.strokes.elementAt(j);
			g.setColor(s.color);
			drawStroke(g, s.points);
		}
		drawStroke(g, points);
	}
	
	public void render2(Graphics g)
	{		
		if(backgroundImg != null)
			g.drawImage(backgroundImg, 0, 30, null);
		
		Enumeration<Drawing> e = drawings.elements();
		while(e.hasMoreElements())
		{
			Drawing d = e.nextElement();
			MultiStroke m;			
			switch(d.ani)
			{
				case FETCH:
					g.drawImage(d.img, d.x, d.y, null);
					break;
				case TWINKLE:
					m = d.getMs();			
					for(int j=0; j<m.strokes.size(); j++)
					{
						Stroke s = m.strokes.elementAt(j);
						Color c = new Color((s.color.getRed()+5)%255, (s.color.getGreen()+15)%255, (s.color.getBlue()+35)%255);
						s.color = c;
						drawStroke(g, s.points, s.color);
					}
					break;
				case ROTATE:
					m = d.getMs();
					Point p = m.getCentroid();
					m.rotateAroundPoint(p);								
					for(int j=0; j<m.strokes.size(); j++)
					{
						Stroke s = m.strokes.elementAt(j);
						drawStroke(g, s.points, s.color);
					}
					break;
				case ANNOTATE:
					g.setFont(new Font("Arial",Font.BOLD,18));
					g.setColor(Color.red);
					g.drawString(d.getName(), d.getX(), d.getY());
					g.setColor(Color.black);
					break;										
			}
		}

		for(int j=0; j<mStroke.strokes.size(); j++)
		{
			Stroke s = mStroke.strokes.elementAt(j);
			drawStroke(g, s.points, s.color);
		}
		drawStroke(g, points, Color.black);
	}
	
	public void render3(Graphics g, int x, int y)
	{
		if(queryPoints == null || queryPoints.size() == 0)
			return;
		
		Rectangle rec = Utils.BoundingBox(queryPoints);
		
		Point c = new Point(rec.X, rec.Y, false);
		c.X = c.X - x;
		c.Y = c.Y - y;
		Utils.TranslateByPoint(queryPoints, c);
				
		Color mainColor = Color.black;
		g.setColor(mainColor);	
		
		g.setColor(Color.blue);
		g.fillRect((int)queryPoints.elementAt(0).X, (int)queryPoints.elementAt(0).Y, 5, 5);
		g.setColor(mainColor);
		
		for(int i = 0; i < queryPoints.size()-1; i++)
		{
			Point p1 = queryPoints.elementAt(i);
			Point p2 = queryPoints.elementAt(i+1);
			
			if(Utils.Distance(p1, p2) < Concepture.connectDistance)
				g.drawLine((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y);
			else
			{
				g.setColor(Color.green);
				g.fillRect((int)p2.X, (int)p2.Y, 5, 5);
				g.setColor(mainColor);
			}
		}
	}

	private void drawStroke(Graphics g, Vector<Point> pnts, Color c)
	{
		if(pnts.size() == 0)
			return;

		g.setColor(c);
		Point p1, p2;

		for (int i = 0; i < pnts.size()-1; i++)
		{
			p1 = pnts.elementAt(i);
			p2 = pnts.elementAt(i+1);
			g.drawLine((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y);
		}
	}

	private void drawStroke(Graphics g, Vector<Point> pnts)
	{
		if(pnts.size() == 0)
			return;

		Point p1, p2;
		Color mainColor = Color.black;
		if(result != null)
			mainColor = result.gest.color;
		g.setColor(mainColor);

		g.setColor(Color.blue);
		g.fillRect((int)pnts.elementAt(0).X, (int)pnts.elementAt(0).Y, 5, 5);
		g.setColor(mainColor);

		for (int i = 0; i < pnts.size()-1; i++)
		{
			p1 = pnts.elementAt(i);
			p2 = pnts.elementAt(i+1);

			if(p2.isBreakpoint())
			{
				g.setColor(Color.red);
				g.fillRect((int)p2.X, (int)p2.Y, 5, 5);
				g.setColor(mainColor);
			}
			g.drawLine((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y);
		}
	}

	public Answer recognizeGesture(boolean shortcut)
	{
		if(mStroke.strokes.size() == 0)
			return null;
		Point first = mStroke.strokes.elementAt(0).points.elementAt(0);
		Point last = mStroke.strokes.lastElement().points.lastElement();
		boolean circular = (Utils.Distance(first, last) < connectDistance);

		if(!shortcut)
		{
			for(int i=0; i<mStroke.strokes.size(); i++)
			{
				Stroke s = mStroke.strokes.elementAt(i);
				int n = (int)(Utils.PathLength(s.points) / 4.0) + 1;
				s.points = Utils.resample(s.points, n);
				Utils.findAnnotations(s.points, 3, 5);
			}
		}
		result = recognizer.recognizeGesture(mStroke, 0.6, circular);
		return result;
	}

	public void recognize()
	{
		Answer ans = recognizeGesture(false);
		if(ans != null)
		{
			Gesture g = ans.getGest();
			int count = ans.getLabel().length();
			mStroke.setColor(g.getColor());
			Point p = mStroke.getCentroid();
			int n = -1;
			
			switch(g.action)
			{
			case FETCH:
				BufferedImage img = loadImage(g.getName(), count);
				if(img != null)
				{
					drawings.add( new Drawing(g.getName(), (int)p.X - img.getWidth()/2, (int)p.Y - img.getHeight()/2,
							g.action, null, img));
					mStroke.clearStrokes();
					points.removeAllElements();
				}
				break;
			case ANNOTATE:			
				drawings.add(new Drawing(g.getName(),(int)p.X, (int)p.Y, g.action, null, null));
				mStroke.clearStrokes();
				points.removeAllElements();
				break;
			case TWINKLE:
			case ROTATE:
				drawings.add(new Drawing(g.getName(), (int)p.X, (int)p.Y, g.action, new MultiStroke(mStroke), null));
				mStroke.clearStrokes();
				points.removeAllElements();
				break;
			case DELETE:
				if(drawings.size() > 0)
					drawings.removeElementAt(findClosest(p));				
				break;
			case BACK:						
				n = findClosest(p);
				if(n >= 0)
				{
					Drawing draw = drawings.remove(n);
					n = Math.max(n-count, 0);
					drawings.insertElementAt(draw, n);
				}			
				break;
			case FORTH:				
				n = findClosest(p);
				if(n >= 0)
				{
					Drawing draw = drawings.remove(n);
					n = Math.min(n+count, drawings.size());
					drawings.insertElementAt(draw, n);
				}	
				break;		
			}
		}
	}
	
	private int findClosest(Point p)
	{
		double maxDistance = Double.MAX_VALUE;
		int closest = -1;
		for(int i=0; i<drawings.size(); i++)
		{
			double d = Utils.Distance(p, new Point(drawings.elementAt(i).getX(),
					drawings.elementAt(i).getY(), false));
			if(d < maxDistance)
			{
				maxDistance = d;
				closest = i;
			}
		}
		return closest;
	}

	public String addExample(boolean shortcut)
	{
		if(!shortcut)
		{
			for(int i=0; i<mStroke.strokes.size(); i++)
			{
				Stroke s = mStroke.strokes.elementAt(i);
				int n = (int)(Utils.PathLength(s.points) / 4.0) + 1;
				s.points = Utils.resample(s.points, n);
				Utils.findAnnotations(s.points, 3, 5);
			}
		}

		Point first = mStroke.strokes.elementAt(0).points.elementAt(0);
		Point last = mStroke.strokes.lastElement().points.lastElement();

		added.clear();	// reset the added templates vector
		String str = recognizer.processGesture(mStroke, added).name;

		newGest.addPositive(str);
		if(Utils.Distance(first, last) > connectDistance)
			newGest.circular = false;		// one counter example is enough to say it's not circular

		return str;
	}

	public String[] getGestures()
	{
		return recognizer.getGestureNames();
	}

	public Object[][] getGestureInfo()
	{
		return recognizer.getGestureInfo();
	}

	public void deleteGesture(int which)
	{
		recognizer.deleteGesture(which);
	}

	public void registerGesture()
	{
		newGest.buildMachine();
		recognizer.addGesture(newGest);
		result = null;
		newGest = null;
	}

	public void initLearning()
	{
		newGest.initMachine();
	}

	public boolean learnGesture(boolean ans) throws RuntimeException
	{
		if(!ans)
			newGest.addNegative(prevStr);		// save it for a rainy day

		while(true)
		{
			String str = newGest.getExample(ans);
			if(str == null)	// learning is done
			{
				if(!newGest.registerMachine())
				{
					newGest = null;
					throw new RuntimeException("Insufficient examples or inconsistent queries");
				}
				recognizer.addGesture(newGest);
				result = null;
				newGest = null;
				return true;
			}
			if(str.equals(""))
			{
				ans = false;
				continue;
			}
			System.out.println("Query string: " + str); // for debugging purposes - to be removed
			queryPoints = recognizer.makeGesture(str, 270, 270, newGest.isCircular());
			
			if(queryPoints == null)			// then do it all again
				ans = false;		// if we can not draw it, it can not be a positive example
			else
			{
				prevStr = str;
				break;
			}
		}
		return false;
	}

	public String getString()
	{
		return prevStr;
	}

	public Answer getName()
	{
		return result;
	}

	public void setNewGesture(String s)
	{
		newGest = new Gesture(s);
	}

	public void setGestureColor(Color c)
	{
		if(newGest != null)
			newGest.setColor(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	public void setGestureAction(Gesture.Animation ga)
	{
		if(newGest != null)
			newGest.setAction(ga);
	}

	public void eraseLastExample()
	{
		if(newGest != null)
		{
			newGest.eraseLast();
			recognizer.eraseTemplates(added);
			added.clear();
		}
	}

	public void pointerPressed(int x, int y)
	{
		if(!entering && !correcting)
		{
			points.removeAllElements();
			mStroke.clearStrokes();
			result = null;
			entering = true;
		}
	}

	public void pointerReleased(int x, int y)
	{
		if(points.size() > 2 && !clicked)	
			mStroke.addStroke(new Stroke(points));
		points.removeAllElements();
	}

	public void pointerDragged(int x, int y)
	{
		points.addElement(new Point(x, y, false));
	}

	public void pointerClicked(int x, int y)
	{
		Point p1 = new Point(x, y, false);
		double minDist = clickDistance;		// maximum distance allowed
		boolean cornerFound = false;
		Point closest = null;

		// if there are any corner points within click distance
		// make them non-corners
		for(int i=0; i<mStroke.strokes.size(); i++)
		{
			Stroke s = mStroke.strokes.elementAt(i);
			for(int j=1; j<s.points.size()-1; j++)
			{
				Point p2 = s.points.elementAt(j);
				double d = Utils.Distance(p1, p2);
				if(d < clickDistance && p2.isBreakpoint())
				{
					cornerFound = true;
					p2.setBreakpoint(false);
				}
				if(d < minDist)
				{
					minDist = d;
					closest = p2;
				}
			}
		}

		if(!cornerFound && closest != null)				// then we add a corner at the closest point
			closest.setBreakpoint(true);
	}

	public boolean saveSession(String filename)
	{
		return recognizer.writeSession(filename);
	}

	public boolean loadSession(String filename)
	{
		return recognizer.readSession(filename);
	}
}
