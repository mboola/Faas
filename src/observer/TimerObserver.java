package observer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	public Long calculateMaxTime(String functionId) {
		List<Long> list = MetricSet.instantiate().getList(metricId, functionId);
			
		Optional<Long> max = calculateMaxMetric(list, Comparator.comparingLong(value -> value));
		if (max.isPresent()) return max.get();
		else return null;
	}
	
}
