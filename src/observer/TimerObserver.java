package observer;

import java.util.Comparator;
import java.util.List;

import core.exceptions.NoResultAvailable;
import core.metrics.Metric;
import core.metrics.MetricCollection;

public class TimerObserver extends Observer {

	private final String		metricId	= "TimerObserver";
	private Metric<Long>		metric;

	@Override
	public void execution() {
		metric = new Metric<Long>(functionId, System.nanoTime());
	}

	@Override
	public void update() {
		Long time = metric.getDataType();
		time = System.nanoTime() - time;
		metric.setDataType(time);

		MetricCollection.instantiate().addMetric(metricId, metric);
	}

	@Override
	public TimerObserver copy() {
		return new TimerObserver();
	}

	public Long calculateMaxTime(String functionId) {
		try {
			List<Long> list = MetricCollection.instantiate().getList(metricId, functionId);
			return calculateMaxMetric(list, Comparator.comparingLong(value -> value));
		}
		catch (NoResultAvailable e) {
			return null;
		}
	}

	public Long calculateMinTime(String functionId) {
		try {
			List<Long> list = MetricCollection.instantiate().getList(metricId, functionId);
			return calculateMinMetric(list, Comparator.comparingLong(value -> value));
		}
		catch (NoResultAvailable e) {
			return null;
		}
	}

	public Long calculateAverageTime(String functionId) {
		try {
			List<Long> list = MetricCollection.instantiate().getList(metricId, functionId);
			return (long) calculateAverageMetric(list, (var) -> var);
		}
		catch (NoResultAvailable e) {
			return null;
		}
	}

	public Long calculateAllTime(String functionId) {
		try {
			List<Long> list = MetricCollection.instantiate().getList(metricId, functionId);
			return (long) calculateAccumulativeMetric(list, 0L, (x, y) -> x + y);
		}
		catch (NoResultAvailable e) {
			return null;
		}
	}
	
}
