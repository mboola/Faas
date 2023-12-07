package application;

import java.io.Serializable;
import java.util.function.Function;

public class Invokable implements Serializable{

    private	String	id;
	private Object	invokable;
    private long	ram;

	public Invokable(String id, Object invokable, long ram) {
		this.ram = ram;
		this.invokable = invokable;
		this.id	= id;
	}

    public String getId()
	{
		return (id);
	}

    public Object getInvokable()
	{
		return (this.invokable);
	}

	public long getRam()
	{
		return (this.ram);
	}

	public <T, R> R apply(T args)
	{
		return (((Function<T, R>)invokable).apply(args));
	}
}
