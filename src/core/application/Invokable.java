package core.application;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A class that represents an invokable function with a unique identifier, the invokable function itself,
 * and the allocated RAM for execution.
 */
public class Invokable<T, R> implements Serializable {

	private Function<T, R> invokable;
	private long ram;

	/**
     * Constructs an Invokable instance with the specified ID, invokable function, and allocated RAM.
     *
     * @param id        The unique identifier for the invokable function.
     * @param invokable The invokable function.
     * @param ram       The allocated RAM for the invokable function.
     */
	public Invokable(Function<T, R> invokable, long ram) {
		this.ram = ram;
		this.invokable = invokable;
	}

	/**
     * Gets the invokable function.
     *
     * @return The invokable function.
     */
    public Function<T, R> getInvokable()
	{
		return (this.invokable);
	}

	/**
     * Gets the allocated RAM for the invokable function.
     *
     * @return The allocated RAM.
     */
	public long getRam()
	{
		return (this.ram);
	}
}