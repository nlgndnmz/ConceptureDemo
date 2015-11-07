package concepture;

import java.io.Serializable;

/**
 * Defines the transition between two states in a automaton.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
class Transition implements Serializable
{
	String emition;
	String toState;
	
	static final long serialVersionUID = 12345;
	
	Transition(String emition, String toState)
	{
		this.emition = new String(emition);
		this.toState = new String(toState);
	}
	
	Transition(Transition src)
	{
		this.emition = new String(src.emition);
		this.toState = new String(src.toState);
	}
	
	boolean contradicts(Transition other)
	{
		if(this.emition.equals(other.emition) && !this.toState.equals(other.toState))
			return true;
		else
			return false;
	}
	
	String getEmition() 
	{
		return emition;
	}

	void setEmition(String emition) 
	{
		this.emition = emition;
	}

	String getStateNum() 
	{
		return toState;
	}

	void setStateNum(String stateID) 
	{
		this.toState = stateID;
	}
}
