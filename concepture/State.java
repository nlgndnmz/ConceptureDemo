package concepture;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Defines the state of an FSA. Required by the Automaton class.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
class State implements Serializable 
{
	String stateID;	
	boolean terminal;
	HashMap<String, Transition> transitions;	
	
	static final long serialVersionUID = 12345;
	
	State(String stateID, boolean terminal)
	{		
		this.setStateID(stateID);
		this.setTerminal(terminal);
		transitions = new HashMap<String, Transition>();
	}
	
	State(State src)
	{
		this.setStateID(src.stateID);
		this.setTerminal(src.terminal);	
		transitions = new HashMap<String, Transition>();		
		inheritTransitions(src);
	}
	
	void printTransitions()
	{	
		Iterator<String> abc = getEmitions();
		while(abc.hasNext())
		{
			String s = abc.next();
			Transition t = transitions.get(s);
			System.out.print("( " + t.emition + " ) " + t.toState + " ; ");				
		}
		System.out.println();
	}
	
	Iterator<String> getEmitions()
	{
		return transitions.keySet().iterator();
	}
	
	void setTerminal(boolean terminal) 
	{
		this.terminal = terminal;
	}

	boolean isTerminal() 
	{
		return terminal;
	}
	
	void addTransition(Transition newTransition)
	{
		transitions.put(newTransition.emition, newTransition);
	}

	void setStateID(String stateID) 
	{
		this.stateID = new String(stateID);
	}

	String getStateID() 
	{
		return stateID;
	}
	
	String consume(String s)
	{						
		if(transitions.containsKey(s))
			return transitions.get(s).toState;	
		return null;
	}
	
	void renameNeighbour(String before, String after)
	{
		Iterator<String> abc = getEmitions();
		while(abc.hasNext())
		{
			String s = abc.next();
			if(transitions.containsKey(s) && transitions.get(s).toState.equals(before))						
				transitions.get(s).toState = after;
		}		
	}
	
	void inheritTransitions(State src)
	{		
		Iterator<String> abc = src.getEmitions();
		while(abc.hasNext())
		{
			String s = abc.next();
			this.transitions.put(s, new Transition(src.transitions.get(s)));
		}
	}
	
	boolean isCompatible(State other)
	{		
		Iterator<String> abc = getEmitions();
		while(abc.hasNext())
		{
			String s = abc.next();
			if(other.transitions.containsKey(s))
			{
				Transition t1 = transitions.get(s);
				Transition t2 = other.transitions.get(s);				
				if(t1.contradicts(t2))
				{
					if(t1.toState.equals(other.stateID) && t2.toState.equals(this.stateID))
					{}
					else
						return false;
				}
			}			
		}
		return true;
	}
}
