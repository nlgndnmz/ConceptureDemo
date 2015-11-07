package concepture;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class FlatButton
{
	int x, y;
	int width;
	int height;	
	boolean pressed;	
	
	public FlatButton(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;				
		this.width = width;
		this.height = height;		
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
        g.setColor(Color.LIGHT_GRAY);			
        g.fillRoundRect(x, y, width, height, width/5, height/5);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, width, height, width/5, height/5);
		g.setFont(new Font("Courier",Font.PLAIN,14));
		g.drawString(s, x + 3, y + height + 15);
	}
	
}
