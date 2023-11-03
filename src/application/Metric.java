package application;

public class Metric<T> {
	private String	id;
	private T		dataType;

	public Metric(String id, T dataType)
	{
		this.id = id;
		this.dataType = dataType;
	}

	public String getId()
	{
		return (id);
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
