package observer;

import application.Metric;
import invoker.InvokerInterface;

public interface Observer {

	public void preinitialize(String id, InvokerInterface invoker) throws Exception;

	public <T> Metric<T> initialize(String id, InvokerInterface invoker) throws Exception;

	public <T> void update(Metric<T> metric);

}
