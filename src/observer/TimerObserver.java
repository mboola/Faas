package observer;

import application.Metric;
import application.MetricSet;
import invoker.InvokerInterface;

public class TimerObserver implements Observer {

	private String		metricId	= "TimerObserver";
	
	public <T> void initialize(String id, InvokerInterface invoker) throws Exception {
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> Metric<T> execution(String id, InvokerInterface invoker)
	{
		return (Metric<T>) (new Metric<Long>(id, System.nanoTime()));
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> void update(Metric<T> metric) {
		Metric<Long> timeMetric = (Metric<Long>)metric;
		Long time = timeMetric.getDataType();
		time = System.nanoTime() - time;
		timeMetric.setDataType(time);

		MetricSet.instantiate().addMetric(metricId, timeMetric);
	}
	
}
