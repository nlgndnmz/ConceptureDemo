package concepture;

import java.util.Vector;
import java.io.Serializable;

/**
 * Defines a possibly multi-stroke template.
 * It logs the name of the template and the number of strokes
 * in addition to the two dimensional points.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
class Template implements Serializable
{
	String name;
	Vector<Point> points;
	
	private int midpoint;
	private boolean doubleStroke;
	private boolean active;
	private int labelLength = 1;
	
	static final long serialVersionUID = 12345;
	
	Template(String name, MultiStroke ms, boolean doubleStroke)
	{		
		this.name = name;
		this.points = new Vector<Point>();				
		
		for(int i=0; i<ms.strokes.size(); i++)
		{					
			Stroke s = new Stroke(ms.strokes.elementAt(i).points);
			s.points = Utils.resample(s.points, Recognizer.NumPoints);			
			this.points.addAll(s.points);
		}
		this.points = Utils.RotateToZero(this.points);	
		Utils.ScaleToUnit(this.points, Recognizer.SquareSize);
		Utils.TranslateToOrigin(this.points);	
		
		this.doubleStroke = doubleStroke;
		this.midpoint = getBreakpoint();		
		this.active = true;
	}
	
	private int getBreakpoint()
	{
		for(int i=points.size()-2; i>2; i--)
		{
			if(points.elementAt(i).isBreakpoint())
				return i;
		}
		return 0;
	}
	
	int getMidpoint()
	{
		return midpoint;
	}

	boolean isActive()
	{
		return active;
	}

	void setActive(boolean active)
	{
		this.active = active;
	}
	
	boolean isDoubleStroke()
	{
		return doubleStroke;
	}

	public int getLabelLength()
    {
    	return labelLength;
    }

	public void setLabelLength(int labelLength)
    {
    	this.labelLength = labelLength;
    }
}
