package concepture;

import java.io.Serializable;

/**
 * Defines a two dimensional point.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
public class Point implements Serializable
{
	double X, Y;
	boolean breakpoint;	
	
	static final long serialVersionUID = 12345;

	public Point(double x, double y, boolean breakpoint)
	{	
		this.X = x; 
		this.Y = y;	
		this.breakpoint = breakpoint;
	}
	
	public Point(Point src)
	{
		this.X = src.X;
		this.Y = src.Y;
		this.breakpoint = src.breakpoint;
	}
	
	public boolean isBreakpoint()
	{
		return breakpoint;
	}

	public void setBreakpoint(boolean breakpoint)
	{
		this.breakpoint = breakpoint;
	}
	
	public double getX()
	{
		return X;
	}

	public void setX(double x)
	{
		X = x;
	}

	public double getY()
	{
		return Y;
	}

	public void setY(double y)
	{
		Y = y;
	}
	
	public void normalize()
	{		
		double len = Math.sqrt(X*X + Y*Y);
		X /= len;
		Y /= len;		
	}
}
