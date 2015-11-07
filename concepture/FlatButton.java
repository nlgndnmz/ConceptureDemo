package concepture;

import java.awt.Font;
import java.awt.Graphics;

public class FlatButton
{
	int x, y;
	int width;
	int height;
	Color c;
	boolean pressed;	
	
	public FlatButton(int x, int y, int width, int height, Color c)
	{
		this.x = x;
		this.y = y;				
		this.width = width;
		this.height = height;
		this.c = c;
	}
	
	public void setIfNear(int x2, int y2)
	{		
		if(x2>x && x2<x+width && y2>y && y2<y+height)
			pressed = true;
	}
	
	public boolean isPressed()
	{
		return pressed;
	}
	
	public void setPressed(boolean pressedValue)
	{
		pressed = pressedValue;
	}
	
	public void drawButton(Graphics g, String s, int y1)
	{		
		y = y1;			
		g.setColor(c)
		g.fillRect(x, y, width, height);
		g.setColor(Color.BLACK)
		g.setFont(new Font("Courier",Font.PLAIN,14));
		g.drawString(s, x + 3, y + height + 15);
	}
	
}
