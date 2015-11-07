package concepture;

public class Stats
{
	String name;
	int numQueries;
	int numCorrections;
	int numExamples;
	int numUndos;
	
	public Stats(String name)
	{
		this.name = name;
		numQueries = 0;
		numCorrections = 0;
		numExamples = 0;
		numUndos = 0;
	}
	
	public void updateNumQueries(int n)
	{
		numQueries += n;
	}
	
	public void updateNumCorrections(int n)
	{
		numCorrections += n;
	}
	
	public void updateNumExamples(int n)
	{
		numExamples += n;
	}
	
	public void updateNumUndos(int n)
	{
		numUndos += n;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumQueries() {
		return numQueries;
	}

	public void setNumQueries(int numQueries) {
		this.numQueries = numQueries;
	}

	public int getNumCorrections() {
		return numCorrections;
	}

	public void setNumCorrections(int numCorrections) {
		this.numCorrections = numCorrections;
	}

	public int getNumExamples() {
		return numExamples;
	}

	public void setNumExamples(int numExamples) {
		this.numExamples = numExamples;
	}

	public int getNumUndos() {
		return numUndos;
	}

	public void setNumUndos(int numUndos) {
		this.numUndos = numUndos;
	}
	
}
