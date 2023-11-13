package application;

import java.util.function.Function;

public class Action{

	private int		ram;
	private Object	function;

	public Action(int ram, Object function) {
		this.ram = ram;
		this.function = function;
	}

	public int getRam()
	{
		return (this.ram);
	}

	public Object getFunction()
	{
		return (this.function);
	}

	public <T, R> R apply(T args)
	{
		Function<T, R>	func = (Function<T, R>) function;
		return (func.apply(args));
	}
}
