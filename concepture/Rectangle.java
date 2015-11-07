package concepture;

/**
 * Defines a rectangle by storing the upper left corner and
 * the width and height.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
public class Rectangle
{
	public double X, Y, Width, Height;

	public Rectangle(double x, double y, double width, double height) // constructor
	{
		this.X = x;
		this.Y = y;
		this.Width = width;
		this.Height = height;
	}

	public void copy(Rectangle src)
	{
		X = src.X;
		Y = src.Y;
		Width = src.Width;
		Height = src.Height;
	}

	public double getHeight()
	{
		return Height;
	}

	public void setHeight(double height)
	{
		Height = height;
	}

	public double getWidth()
	{
		return Width;
	}

	public void setWidth(double width)
	{
		Width = width;
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
}
