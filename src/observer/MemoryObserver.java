package observer;

import application.Controller;
import application.Metric;
import invoker.InvokerInterface;

public class MemoryObserver implements Observer {

	private Controller	controller;
	private String		metricId	= "MemoryObserver";

	public void preinitialize(String id, Controller controller, InvokerInterface invoker){
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> Metric<T> initialize(String id, Controller controller, InvokerInterface invoker) throws Exception
	{
		this.controller = controller;
		return (Metric<T>) (new Metric<Long>(id, invoker.getRamUsed()));
	}

	@Override
	public <T> void update(Metric<T> metric) {
		//TODO change the way I access this
		controller.metrics.addMetric(metricId, metric);
	}

}
