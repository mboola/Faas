package observer;

import application.Controller;
import application.Metric;
import invoker.InvokerInterface;

public class InvocationObserver implements Observer {

	private String	metricId = "InvocationObserver";

	public void preinitialize(String id, Controller controller, InvokerInterface invoker) throws Exception{
		System.out.println("!!!!!Invoker selected: " + invoker.getId());
		controller.metrics.addMetric(metricId, new Metric<String>(id, invoker.getId()));
	}

	@Override
	public <T> Metric<T> initialize(String id, Controller controller, InvokerInterface invoker) {
		return (null);
	}

	@Override
	public <T> void update(Metric<T> metric) {
	}

}
