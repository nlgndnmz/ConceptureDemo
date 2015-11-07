package concepture;

import java.awt.image.BufferedImage;

public class Drawing
{
	BufferedImage img;
	String name;
	int x;
	int y;
	MultiStroke ms;
	Gesture.Animation ani;
	
	public Drawing(String name, int x, int y, Gesture.Animation ani, MultiStroke ms, BufferedImage img)
	{
		this.name = name;
		this.img = img;
		this.x = x;
		this.y = y;
		this.ms = ms;
		this.ani = ani;
	}

	public BufferedImage getImg() 
	{
		return img;
	}

	public void setImg(BufferedImage img) 
	{
		this.img = img;
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public int getX() 
	{
		return x;
	}

	public void setX(int x) 
	{
		this.x = x;
	}
	
	public MultiStroke getMs()
	{
		return ms;
	}

	public int getY() 
	{
		return y;
	}

	public void setY(int y) 
	{
		this.y = y;
	}
	
}
