package application;

public class PairValues {

	private Object args;
	private Object result;

	public PairValues(Object args, Object result)
	{
		this.args = args;
		this.result = result;
	}

	public Object getArgs() 
	{
		return args;
	}

	public Object getResult() 
	{
		return result;
	}
}
