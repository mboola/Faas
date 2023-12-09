package observer;

import application.Controller;
import application.Metric;
import invoker.InvokerInterface;

public interface Observer {

	public void preinitialize(String id, Controller controller, InvokerInterface invoker) throws Exception;

	public <T> Metric<T> initialize(String id, Controller controller, InvokerInterface invoker) throws Exception;

	public <T> void update(Metric<T> metric);

}
