package core.exceptions;

/**
 * Checked exception indicating that an operation is not valid due to a null value,
 * when trying to add a value to a set that already contains said value or
 * when trying to delete a value from a set that doesn't contain said value.
 * <p>
 * This exception extends the standard Java {@link Exception} class.
 * 
 * @see Exception
 */
public class OperationNotValid extends Exception {

	/**
	 * Constructs an OperationNotValid with the specified detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
	 */
	public OperationNotValid(String message) {
		super(message);
	}

}
