package application;

import java.io.Serializable;

/**
 * A class that represents an invokable function with a unique identifier, the invokable function itself,
 * and the allocated RAM for execution.
 */
public class Invokable implements Serializable{

	private String id;
    private Object invokable;
    private long ram;

	/**
     * Constructs an Invokable instance with the specified ID, invokable function, and allocated RAM.
     *
     * @param id        The unique identifier for the invokable function.
     * @param invokable The invokable function.
     * @param ram       The allocated RAM for the invokable function.
     */
	public Invokable(String id, Object invokable, long ram) {
		this.ram = ram;
		this.invokable = invokable;
		this.id	= id;
	}

    /**
     * Gets the ID of the invokable function.
     *
     * @return The unique identifier.
     */
	public String getId()
	{
		return (id);
	}

	/**
     * Gets the invokable function.
     *
     * @return The invokable function.
     */
    public Object getInvokable()
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
