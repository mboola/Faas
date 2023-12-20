package observer;

import core.invoker.InvokerInterface;
import core.metrics.Metric;
import core.metrics.MetricSet;

public class MemoryObserver implements Observer {

	private String		metricId	= "MemoryObserver";

	public <T> void initialize(String id, InvokerInterface invoker) throws Exception
	{
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> Metric<T> execution(String id, InvokerInterface invoker) throws Exception
	{
		return (Metric<T>) (new Metric<Long>(id, invoker.getUsedRam()));
	}

	@Override
	public <T> void update(Metric<T> metric) {
		MetricSet.instantiate().addMetric(metricId, metric);
	}

}
