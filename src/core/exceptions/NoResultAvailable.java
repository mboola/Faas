package core.exceptions;

/**
 * Checked exception indicating that no result is available for a specific operation or query in the context of the Cache class.
 * This exception is typically thrown by the {@link decorator.Cache#getCacheResult(String, Object)} method when no matching arguments
 * have been found in the cache.
 * Also used with MetricCollection when searching Metrics that hasn't been stored {@link core.metrics.MetricCollection#getList(String metricId, String functionId)}.
 * <p>
 * This exception extends the standard Java {@link Exception} class.
 *
 * @see decorator.Cache
 * @see core.metrics.MetricCollection
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
