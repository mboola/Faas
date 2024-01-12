package core.exceptions;

/**
 * Checked exception indicating that there is no available invoker for a particular operation.
 * This exception is commonly used in various policy managers when selecting an invoker for execution.
 * <p>
 * This exception extends the standard Java {@link Exception} class.
 * 
 * @see policymanager.PolicyManager
 * @see Exception
 */
public class NoInvokerAvailable extends Exception {

	/**
	 * Constructs a NoInvokerAvailable with the specified detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
	 */
	public NoInvokerAvailable(String message) {
		super(message);
	}

}
