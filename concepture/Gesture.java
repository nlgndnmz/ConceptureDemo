package concepture;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

/**
 * Defines a gesture by means of an automaton. It also logs
 * positive and negative examples given by the user and the user 
 * defined color for the gesture. 
 * 
 * NOTE: This class does not keep nor has access to the actual strokes,
 * the examples are stored as strings of template labels (i.e. letters
 * of the latin alphabet).  
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
public class Gesture implements Serializable
{
	public enum Animation {FETCH, DELETE, BACK, FORTH, ANNOTATE, TWINKLE, ROTATE}
	
	String name;
	Automaton machine;
	Vector<String> positives;
	Vector<String> negatives;
	Color color;	
	Animation action;
	
	boolean circular = true;	// by default it is assumed circular
	
	private HashMap<String, Boolean> known;
	private Vector<String> queries;
	private int nextQuery = 0; 
	private String prevString = "";
	
	static final long serialVersionUID = 12345;
	
	public Gesture(String name)
	{
		this.name = name;
		positives = new Vector<String>(0);
		negatives = new Vector<String>(0);
		action = Animation.FETCH;
	}
	
	public void setColor(int r, int g, int b)
	{
		color = new Color(r,g,b);
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public Animation getAction() 
	{
		return action;
	}

	public void setAction(Animation action) 
	{
		this.action = action;
	}

	public String getName()
	{
		return name;
	}
	
	public boolean isCircular()
	{
		return circular;
	}

	public void addPositive(String str) 
	{
		positives.addElement(str);
	}
	
	public void addNegative(String str)
	{
		negatives.addElement(str);
	}
	
	public void eraseLast()
	{
		if(positives.size() > 0)
		{
			positives.removeElementAt(positives.size()-1);
		}
	}
	
	public void initMachine()
	{
		machine = new Automaton();		
		known = new HashMap<String, Boolean>();
		for(int i=0; i<positives.size(); i++)
			known.put(positives.elementAt(i), Boolean.TRUE);
		machine.buildPart1(positives);
		prevString = "";
		queries = machine.buildPart2a(prevString);			
		nextQuery = 0;		
	}
	
	public String getExample(boolean answer)
	{
		if(nextQuery > 0)	// this will not be true for the very first call!!
			known.put(queries.elementAt(nextQuery-1), Boolean.valueOf(answer));
		
		while(true)
		{			
			if(nextQuery >= queries.size())	// we have finished processing all the examples
			{
				prevString = machine.buildPart2b(known, prevString);
				if(prevString == null)		// language is learned
					return null;
				queries = machine.buildPart2a(prevString);
				nextQuery = 0;
			}
			
			nextQuery++;
			if(!known.containsKey( queries.elementAt(nextQuery-1) ))
				return queries.elementAt(nextQuery-1); 
		}		
	}
	
	public boolean registerMachine()
	{
		if(machine.buildPart3())
		{
			machine.printStates();
			return true;
		}
		return false;
	}
	
	public void buildMachine()
	{
		machine = new Automaton(positives, negatives);					
	}
	
	public boolean isMember(String str)
	{
		if(str.equals(""))		// no language can have the empty string
			return false;
		return machine.simulate(str);
	}

}
