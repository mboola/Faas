package observer;

import application.Controller;
import application.Metric;
import application.MetricSet;
import invoker.InvokerInterface;

public class InvocationObserver implements Observer {

	private String	metricId = "InvocationObserver";

	public void preinitialize(String id, InvokerInterface invoker) throws Exception
	{
		MetricSet.instantiate().addMetric(metricId, new Metric<String>(id, invoker.getId()));
	}

	@Override
	public <T> Metric<T> initialize(String id, InvokerInterface invoker) {
		return (null);
	}

	@Override
	public <T> void update(Metric<T> metric) {
	}

}
