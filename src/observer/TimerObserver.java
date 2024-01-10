package observer;

import java.util.Comparator;
import java.util.List;

import core.metrics.Metric;
import core.metrics.MetricSet;

public class TimerObserver extends Observer {

	private final String		metricId	= "TimerObserver";
	private Metric<Long>		metric;

	@Override
	public void execution() {
		metric = new Metric<Long>(id, System.nanoTime());
	}

	@Override
	public void update() {
		Long time = metric.getDataType();
		time = System.nanoTime() - time;
		metric.setDataType(time);

		MetricSet.instantiate().addMetric(metricId, metric);
	}

	@Override
	public TimerObserver copy() {
		return new TimerObserver();
	}

	public Long calculateMaxTime(String functionId) {
		List<Long> list = MetricSet.instantiate().getList(metricId, functionId);
		if (list == null) return null;

		return calculateMaxMetric(list, Comparator.comparingLong(value -> value));
	}

	public Long calculateMinTime(String functionId) {
		List<Long> list = MetricSet.instantiate().getList(metricId, functionId);
		if (list == null) return null;

		return calculateMinMetric(list, Comparator.comparingLong(value -> value));
	}

	public Long calculateAverageTime(String functionId) {
		List<Long> list = MetricSet.instantiate().getList(metricId, functionId);
		if (list == null) return null;

		return (long) calculateAverageMetric(list, (var) -> var);
	}

	public Long calculateAllTime(String functionId) {
		List<Long> list = MetricSet.instantiate().getList(metricId, functionId);
		if (list == null) return null;

		return (long) calculateAccumulativeMetric(list, 0L, (x, y) -> x + y);
	}
	
}
