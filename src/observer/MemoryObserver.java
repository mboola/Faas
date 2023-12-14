package observer;

import application.Metric;
import application.MetricSet;
import invoker.InvokerInterface;

public class MemoryObserver implements Observer {

	private String		metricId	= "MemoryObserver";

	public void preinitialize(String id, InvokerInterface invoker){
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> Metric<T> initialize(String id, InvokerInterface invoker) throws Exception
	{
		return (Metric<T>) (new Metric<Long>(id, invoker.getUsedRam()));
	}

	@Override
	public <T> void update(Metric<T> metric) {
		MetricSet.instantiate().addMetric(metricId, metric);
	}

}
