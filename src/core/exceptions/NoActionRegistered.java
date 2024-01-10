package core.exceptions;

/**
 * Checked exception indicating that no action is registered for a specified ID.
 * This exception can only be thrown by the Controller when attempting to retrieve an action with an ID
 * that has not been registered, or other methods that use this method.
 * This particular method is: {@link core.application.Controller#getInvokable(String)}.
 * <p>
 * This exception extends the standard Java {@link Exception} class.
 *
 * @see core.application.Controller
 * @see Exception
 */
public class NoActionRegistered extends Exception {

	/**
	 * Constructs a NoActionRegistered with the specified detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
	 */
	public NoActionRegistered(String message) {
		super(message);
	}

}
