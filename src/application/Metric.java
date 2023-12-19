package application;

/**
 * A generic class to define and save metrics from a function. 
 */
public class Metric<T> {
	private String	functionId;
	private T		dataType;

	/**
     * Constructs a newMetric with the specified function identifier and data type.
     *
     * @param functionId The identifier of the associated function.
     * @param dataType   The data type of the metric.
     */
	public Metric(String functionId, T dataType)
	{
		this.functionId = functionId;
		this.dataType = dataType;
	}

	/**
	 * Gets the function ID associated with this instance.
	 *
	 * @return The function ID.
	 */
	public String getFunctionId() {
		return functionId;
	}

	/**
	 * Gets a string representation of the data type associated with this instance.
	 *
	 * @return A string representation of the data type.
	 */
	public String getDataStr() {
		return dataType.toString();
	}

	/**
	 * Gets the data type associated with this instance.
	 *
	 * @return The data type.
	 */
	public T getDataType() {
		return dataType;
	}

	/**
	 * Sets the data type for this instance.
	 *
	 * @param dataType The data type to be set.
	 */
	public void setDataType(T dataType) {
		this.dataType = dataType;
	}
}
