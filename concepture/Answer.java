package concepture;

/**
 * Contains the score and name of a recognized gesture.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
public class Answer 
{
	String name;
	String label;
	double score;
	Gesture gest;
	
	public Answer(String name, double score)
	{
		this.name = name;
		this.score = score;
		this.label = "";
		this.gest = null;
	}
	
	public Answer(String name, double score, String label, Gesture gest)
	{
		this.name = name;
		this.score = score;
		this.label = label;
		this.gest = gest;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String label)
	{
		this.label = label;
	}

	public double getScore()
	{
		return score;
	}

	public void setScore(double score)
	{
		this.score = score;
	}
	
	public Gesture getGest()
	{
		return gest;
	}
}
