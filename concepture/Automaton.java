package concepture;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Defines a Finite State Automaton. It features a learning algorithm
 * using positive and negative examples and also a simulator to decide whether
 * a given string is in the language recognized by the FSA. 
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
public class Automaton implements Serializable
{
	HashMap<String, State> states;	// states
	State root;				// start state
	HashSet<String> alphabet;	// alphabet
	int nextLabel;
	
	private String[] abc;
	private String[] prefices;
	private HashMap<String, HashSet<String>> T;
	String dead = "+";		// "+" will denote the dead (i.e. black hole) state	
	
	static final long serialVersionUID = 12345;
	
	public Automaton()
	{
		states = new HashMap<String, State>();	
		alphabet = new HashSet<String>();
		nextLabel = 0;
		root = null;
	}
		
	public Automaton(Vector<String> positives, Vector<String> negatives)
	{
		states = new HashMap<String, State>();	
		alphabet = new HashSet<String>();
		build(positives, negatives);
	}
	
	public Automaton(Automaton src)
	{
		this.states = new HashMap<String, State>();
		this.alphabet = new HashSet<String>();
		
		Iterator<String> iter = src.getStateIterator();		
		while(iter.hasNext())
		{
			String s = iter.next();
			this.addState(s, new State(src.getState(s)));
		}
		
		this.root = this.getState(src.root.stateID);
		this.alphabet.addAll(src.alphabet);		
		this.nextLabel = src.nextLabel;
	}
	
	public void printStates()
	{			    
		Iterator<String> iter = getStateIterator();		
		while(iter.hasNext())
		{
			String s = iter.next();
			State st = getState(s);
			if(st.terminal)
				System.out.print("term " + s + " -> ");
			else
				System.out.print("state " + s + " -> ");
			st.printTransitions();							
		}
		System.out.println();
	}
	
	private void addState(String s, State newState)
	{
		states.put(s, newState);		
	}
	
	private State getState(String s)
	{
		return states.get(s);
	}
	
	private Iterator<String> getStateIterator()
	{
		return states.keySet().iterator();
	}
	
	public String[] getAlphabet()
	{
		String[] strArr = new String[alphabet.size()];
		alphabet.toArray(strArr);
		return strArr;
	}
	
	private void removeState(String st)
	{
		states.remove(st);
	}
	
	private boolean isState(String st)
	{
		return states.containsKey(st);
	}
	
	public boolean testExamples(Vector<String> negatives)
	{
		for(int i=0; i<negatives.size(); i++)
		{
			if( simulate(negatives.elementAt(i)) )		// if the negative example passes the test
				return false;
		}
		return true;	// all examples failed the test as desired
	}
	
	public boolean simulate(String str)
	{		
		State current = root;
		for(int i=0; i<str.length(); i++)
		{
			String s = str.substring(i, i+1);
			String next = current.consume(s);
			if(next == null)
				return false;
			else
				current = getState(next);
		}
		// if we have reached here we have successfully consumed the string
		// finally we have to make sure the last state we reach is a terminal one
		return current.isTerminal();
	}	
	
	public void buildPart1(Vector<String> positives)
	{				
		HashSet<String> alphabet = new HashSet<String>();
		HashSet<String> nodes = new HashSet<String>();
		nodes.add("");	// for the algorithm to work we need the empty string and the dead state
		nodes.add(dead);
				
		for(int i=0; i<positives.size(); i++)
		{
			String s = positives.elementAt(i);
			for(int j=1; j<=s.length(); j++)
			{
				String t = s.substring(0, j);
				String k = s.substring(j-1, j);
				alphabet.add(k);
				nodes.add(t);
			}
		}	
		
		abc = new String[alphabet.size()];
		alphabet.toArray(abc);
		
		prefices = new String[nodes.size()];
		nodes.toArray(prefices);
		
		T = new HashMap<String, HashSet<String>>();		
		for(int i=0; i<prefices.length; i++)
		{
			String u = prefices[i];
			if(!T.containsKey(u))
				T.put(u, new HashSet<String>());			
			
			if(u.equals(dead))	
				continue;
			
			for(int j=0; j<abc.length; j++)
			{
				String u1 = u + abc[j];
				if(!T.containsKey(u1))				
					T.put(u1, new HashSet<String>());								
			}						
		}					
	}
	
	public Vector<String> buildPart2a(String v)
	{						
		Vector<String> queries = new Vector<String>();
		Iterator<String> iter = T.keySet().iterator();
		while(iter.hasNext())
		{
			String u = iter.next();
			if(u.equals(dead))
				continue;		
			queries.add(u + v);
		}						
		return queries;
	}
	
	public String buildPart2b(HashMap<String, Boolean> queries, String v)
	{										
		Iterator<String> iter = T.keySet().iterator();
		while(iter.hasNext())
		{
			String u = iter.next();
			if(u.equals(dead))
				continue;
			if(queries.get(u + v).equals(Boolean.TRUE))	
				T.get(u).add(v);
		}			
		return findDifferentiator(T, prefices, abc, dead);				
	}
	
	public boolean buildPart3()
	{		 
		// now build the canonical acceptor as the final automaton
		HashMap<String, String> stateNames = new HashMap<String, String>();
		for(int i=0; i<prefices.length; i++)
		{
			String u1 = prefices[i];
			if(stateNames.containsKey(u1))	
				continue;
			
			String next = Integer.toString(nextLabel);
			nextLabel += 1;
			stateNames.put(u1, next);
			
			State st = new State(next, false);
			if(T.get(u1).contains(""))		// all sets containing the empty string
				st.setTerminal(true);				// are terminals				
			addState(next, st);				
							
			Iterator<String> iter = T.keySet().iterator();
			while(iter.hasNext())
			{
				String u2 = iter.next();
				if(u2.equals(u1))
					continue;
				if(compareSets(T.get(u1), T.get(u2)))					
					stateNames.put(u2, next);					
			}							
		}
		root = getState(stateNames.get(""));	// set the root
		
		// finally set the transitions
		for(int i=0; i<prefices.length; i++)
		{
			String u1 = prefices[i];
			State st = getState(stateNames.get(u1));
			for(int j=0; j<abc.length; j++)
			{					
				if(st.consume(abc[j]) == null)	
				{
					if(u1.equals(dead))				
						st.addTransition(new Transition(abc[j], st.stateID));		
					else				
					{
						if(stateNames.containsKey(u1 + abc[j]))
							st.addTransition(new Transition(abc[j], stateNames.get(u1 + abc[j])));
						else
							return false;	// insufficient positive examples or conflicting answers
					}
				}
			}
		}
		// note: the final automaton will contain a dead state, whether it's reachable or not
		return true;
	}
	
	private String findDifferentiator(HashMap<String, HashSet<String>> T, String[] prefices, String[] abc, String dead)	
	{
		for(int i=1; i<prefices.length; i++)
		{
			String s1 = prefices[i];
			for(int j=0; j<i; j++)
			{
				String s2 = prefices[j];					
				if(compareSets(T.get(s1), T.get(s2)))
				{
					// then this pair is a candidate						
					for(int k=0; k<abc.length; k++)
					{
						String ss1 = s1 + abc[k];	// since s1 and s2 are elements of prefices, ss1 and ss2 are guaranteed to be in T 
						String ss2 = s2 + abc[k];
						
						if(s1.equals(dead))
							ss1 = dead;
						
						if(s2.equals(dead))
							ss2 = dead;
						
						if(!compareSets(T.get(ss1), T.get(ss2)))
						{
							String w = chooseDiff(T.get(ss1), T.get(ss2));							
							return abc[k] + w;
						}
					}
				}					
			}
		}
		return null;	// means that we could not find a pair
	}
	
	private boolean compareSets(HashSet<String> set1, HashSet<String> set2)
	{
		if(set1.size() != set2.size())
			return false;
		
		Iterator<String> iter = set1.iterator();
		while(iter.hasNext())
		{
			if(!set2.contains(iter.next()))
				return false;
		}
		return true;		// i.e. they contain the same elements
	}
	
	private String chooseDiff(HashSet<String> set1, HashSet<String> set2)
	{
		Iterator<String> iter = set1.iterator();
		while(iter.hasNext())
		{
			String s = iter.next();
			if(!set2.contains(s))
				return s;
		}
		// if not returned yet try set1 instead
		Iterator<String> iter2 = set2.iterator();
		while(iter2.hasNext())
		{
			String s = iter2.next();
			if(!set1.contains(s))
				return s;
		}
		return null;	// should not reach here!!
	}
	
	
	private void build(Vector<String> positives, Vector<String> negatives)
	{
		root = new State("0", false);
		states.put("0", root);		
		nextLabel = 1;
		for(int i=0; i<positives.size(); i++)
			process(positives.elementAt(i));
		shrink(negatives);
	}
	
	private void process(String str)
	{
		State current = root;
		for(int i=0; i<str.length(); i++)
		{
			String s = str.substring(i, i+1);
			alphabet.add(s);
			String next = current.consume(s);
			if(next == null)	// then we will have to add a new state
			{			
				next = Integer.toString(nextLabel);				
				addState(next, new State(next, false));
				nextLabel += 1;				
				current.addTransition(new Transition(s, next));
			}
			current = states.get(next);
		}
		current.setTerminal(true);	// since we are done with the string
	}
	
	private void merge(String st1, String st2)
	{
		if(!isState(st1) || !isState(st2))	// return if either of the states does not exist anymore
			return;
		
		if(st1.equals(st2))		// do not try to merge the same states
			return;
		
		// anything pointing to state2 now should point to state1		
		Iterator<String> names = getStateIterator();
		while(names.hasNext())		
			getState(names.next()).renameNeighbour(st2, st1);		
		
		// then state1 should inherit the transitions of state2	
		getState(st1).inheritTransitions(getState(st2));
		if( getState(st2).terminal )		// if state2 was a terminal then so should be state1
			getState(st1).terminal = true;	
		
		if(root.stateID.equals(st2))
			root = getState(st1);			// update the root
		
		removeState(st2);							
	}
	
	private boolean checkStates(String st1, String st2)
	{
		if(!isState(st1) || !isState(st2) || st1.equals(st2))
			return false;			
		return getState(st1).isCompatible(getState(st2));
	}
	
	/**
	 * Shrinks (i.e. generalizes) the automaton given one or more negative examples.
	 * @param negatives - a Vector of Strings that are not in the target language
	 */
	private void shrink(Vector<String> negatives)
	{
		Vector<String> que = rankStates();		
		for(int i=que.size()-1; i>0; i--)
		{
			String st1 = que.elementAt(i);
			for(int j=i-1; j>=0; j--)	
			{
				String st2 = que.elementAt(j);
				if( checkStates(st2, st1) )		// make sure merging the states won't produce an NFA
				{
					Automaton newMachine = new Automaton(this);	// deep copies everything
					newMachine.merge(st2, st1);
					if( newMachine.testExamples(negatives) )
					{
						this.states = newMachine.states;
						break;
					}
				}	
			}
		}
	}
	
	/**
	 * Ranks the states based on their lexicographical order. 
	 * The rank is defined as the alphabetically first string to reach the state. 
	 * @return A Vector of Strings corresponding to the states ordered by their rank
	 */
	private Vector<String> rankStates()
	{
		String[] strArr = getAlphabet();	
		Arrays.sort(strArr);	// important: the emitions should be traversed in alphabetical order
		
		Vector<String> que = new Vector<String>(states.size());		
		HashMap<String, String> letters = new HashMap<String, String>();
		int qStart = 0;		
		
		que.add(root.stateID);	
		letters.put(root.stateID, null);
		State current = null;
		while(qStart < que.size())
		{
			current = getState( que.elementAt(qStart) );
			qStart += 1;		
			for(int j=0; j<alphabet.size(); j++)
			{					
				String next = current.consume(strArr[j]);												
				if(next != null && !letters.containsKey(next))
				{
					que.add(next);
					letters.put(next, strArr[j]);
				}				
			}
		}		
		return que;
	}
}
