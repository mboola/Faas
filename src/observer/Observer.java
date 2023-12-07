package observer;

import application.Controller;
import application.Metric;
import invoker.InvokerInterface;

public interface Observer {

	public <T> Metric<T> initialize(String id, Controller controller, InvokerInterface invoker) throws Exception;

	public <T> void update(Metric<T> metric);

}
