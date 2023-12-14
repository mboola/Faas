package observer;

import application.Metric;
import invoker.InvokerInterface;

public interface Observer {

	/*
	 * Called when assigned.
	 */
	public <T> void initialize(String id, InvokerInterface invoker) throws Exception;

	/*
	 * Called before execution.
	 */
	public <T> Metric<T> execution(String id, InvokerInterface invoker) throws Exception;

	/*
	 * Called after execution.
	 */
	public <T> void update(Metric<T> metric);

}
