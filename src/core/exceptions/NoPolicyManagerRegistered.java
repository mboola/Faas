package core.exceptions;

/**
 * Checked exception indicating that no policy manager is registered for a particular operation.
 * This exception is commonly thrown when an operation requires a policy manager, but none is available.
 * <p>
 * This exception extends the standard Java {@link Exception} class.
 * 
 * @see Exception
 */
public class NoPolicyManagerRegistered extends Exception {

	/**
	 * Constructs a NoPolicyManagerRegistered with the specified detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
	 */
	public NoPolicyManagerRegistered(String message) {
		super(message);
	}

}
