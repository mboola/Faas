package core.metrics;

/**
 * A generic class representing a metric associated with a function and its corresponding data type.
 * This class allows the storage and retrieval of information related to metrics.
 *
 * @param <T> the type of data associated with the metric
 */
public class Metric<T> {

	private String	functionId;
	private T		dataType;

	/**
     * Constructs a Metric with the specified function ID and data type.
     *
     * @param functionId the identifier of the associated function
     * @param dataType   the data type associated with the metric
     */
	public Metric(String functionId, T dataType)
	{
		this.functionId = functionId;
		this.dataType = dataType;
	}

	/**
     * Gets the function ID associated with the metric.
     *
     * @return the function ID
     */
	public String getFunctionId() {
		return functionId;
	}

	/**
     * Gets the string representation of the data associated with the metric.
     *
     * @return the string representation of the data
     */
	public String getDataStr() {
		return dataType.toString();
	}

	 /**
     * Gets the data type associated with the metric.
     *
     * @return the data type
     */
	public T getDataType() {
		return dataType;
	}

	/**
     * Sets the data type associated with the metric.
     *
     * @param dataType the data type to be set
     */
	public void setDataType(T dataType) {
		this.dataType = dataType;
	}

}
