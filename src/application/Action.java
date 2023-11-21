package application;

import java.util.function.Function;

public class Action{

	private int		ram;
	private Object	function;
	private	String	id;

	public Action(int ram, Object function, String id) {
		this.ram = ram;
		this.function = function;
		this.id	= id;
	}

	public int getRam()
	{
		return (this.ram);
	}

	public Object getFunction()
	{
		return (this.function);
	}

	public String getId()
	{
		return (id);
	}

	public <T, R> R apply(T args)
	{
		Function<T, R>	func = (Function<T, R>) function;
		return (func.apply(args));
	}
}
