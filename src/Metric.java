public class Metric<T, R> {
	private String	id;
	private T		args;
	private long 	timeExecution;

	public Metric(String id, T args)
	{
		this.id = id;
		this.args = args;
		this.timeExecution = System.currentTimeMillis();
	}

	public void updateTime(long time)
	{
		timeExecution = time - timeExecution;
	}

	public String getId()
	{
		return (id);
	}

	public long getTime()
	{
		return (timeExecution);
	}
}
