package application;

public class Metric<T> {
	private String	functionId;
	private T		dataType;

	public Metric(String functionId, T dataType)
	{
		this.functionId = functionId;
		this.dataType = dataType;
	}

	public String getFunctionId()
	{
		return (functionId);
	}

	public String printData()
	{
		return (dataType.toString());
	}

	public T getDataType()
	{
		return (dataType);
	}

	public void setDataType(T dataType)
	{
		this.dataType = dataType;
	}
}
