package observer;

import application.Metric;
import application.MetricSet;
import invoker.InvokerInterface;

public class IdObserver implements Observer {

	private String		metricId	= "IdObserver";

	public void preinitialize(String id, InvokerInterface invoker) throws Exception{
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> Metric<T> initialize(String id, InvokerInterface invoker) throws Exception
	{
		return (Metric<T>) (new Metric<String>(id, invoker.getId()));
	}

	@Override
	public <T> void update(Metric<T> metric) {
		MetricSet.instantiate().addMetric(metricId, metric);
	}

}
