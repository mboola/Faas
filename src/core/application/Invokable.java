package core.application;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Represents an invokable function with the allocated RAM memory needed to execute
 * and the function itself.
 *
 * @param <T> the type of the input to the invokable function
 * @param <R> the type of the result of the invokable function
 *
 * @see Serializable
 */
public class Invokable<T, R> implements Serializable {

	private Function<T, R> invokable;
	private long ram;

	/**
     * Constructs an Invokable instance with the specified invokable function and allocated RAM.
     *
     * @param invokable the invokable function to be associated with this object
     * @param ram the amount of RAM (in bytes) associated with the invokable function
     */
	public Invokable(Function<T, R> invokable, long ram) {
		this.ram = ram;
		this.invokable = invokable;
	}

	/**
     * Gets the invokable function.
     *
     * @return the invokable function
     */
	public Function<T, R> retrieveInvokable()
	{
		return (this.invokable);
	}

	/**
     * Gets the amount of RAM allocated (in megabytes) for the invokable function.
     *
     * @return the amount of RAM
     */
	public long getRam()
	{
		return (this.ram);
	}
}
