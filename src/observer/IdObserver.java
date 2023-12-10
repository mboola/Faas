package observer;

import application.Controller;
import application.Metric;
import invoker.InvokerInterface;

public class IdObserver implements Observer {

	private Controller	controller;
	private String		metricId	= "IdObserver";

	public void preinitialize(String id, Controller controller, InvokerInterface invoker) throws Exception{
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> Metric<T> initialize(String id, Controller controller, InvokerInterface invoker) throws Exception
	{
		this.controller = controller;
		return (Metric<T>) (new Metric<String>(id, invoker.getId()));
	}

	@Override
	public <T> void update(Metric<T> metric) {
		controller.addMetric(metricId, metric);
	}

}
