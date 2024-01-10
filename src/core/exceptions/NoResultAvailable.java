package core.exceptions;

/**
 * Checked exception indicating that no result is available for a specific operation or query in the context of the Cache class.
 * This exception is typically thrown by the {@link decorator.Cache#getCacheResult(String, Object)} method when no matching arguments
 * have been found in the cache.
 *
 * @see decorator.Cache
 * @see Exception
 */
public class NoResultAvailable extends Exception {

	/**
	 * Constructs a NoResultAvailable with the specified detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
	 */
	public NoResultAvailable(String message) {
		super(message);
	}

}
