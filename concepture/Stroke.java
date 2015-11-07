package concepture;

import java.awt.Color;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Defines a stroke by a vector of two dimensional points.
 * It also keeps track of the annotated corners and a user defined color.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
class Stroke 
{
	Vector<Point> points;
	Color color;
	
	Stroke()
	{
		points = new Vector<Point>();
		color = new Color(0,0,0);
	}
	
	Stroke(Stroke src)
	{
		color = new Color(src.color.getRed(), src.color.getGreen(), src.color.getBlue());
		this.points = new Vector<Point>(src.points.size());
		for(int i=0; i<src.points.size(); i++)
			this.points.addElement(new Point(src.points.elementAt(i)));
	}
	
	Stroke(Vector<Point> p, Color c)
	{
		color = new Color(c.getRed(), c.getGreen(), c.getBlue());
		points = new Vector<Point>(p.size());
		for(int i=0; i<p.size(); i++)
			points.addElement(new Point(p.elementAt(i)));
	}
	
	Stroke(Vector<Point> p)
	{
		color = new Color(0,0,0);
		points = new Vector<Point>(p.size());
		for(int i=0; i<p.size(); i++)
			points.addElement(new Point(p.elementAt(i)));
	}
	
	void setColor(Color c)
	{
		color = new Color(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	Color getColor()
	{
		return color;
	}
	
	Vector<Point> getPoints(boolean reverse)
	{
		Vector<Point> newPoints = new Vector<Point>(points.size());
		Enumeration<Point> e = points.elements();
		while(e.hasMoreElements())
		{
			Point p = new Point(e.nextElement());
			if(reverse)
				p.X = 0 - p.X;
			newPoints.add(p);
		}
		return newPoints;
	}
}
