package observer;

import java.util.Comparator;
import java.util.List;

import core.exceptions.NoResultAvailable;
import core.invoker.InvokerInterface;
import core.metrics.Metric;
import core.metrics.MetricCollection;

/**
 * Observer implementation for monitoring the execution time of invocations.
 * It captures and analyzes metrics related to the time taken for the execution of the observed function.
 * <p>
 * This observer extends the {@link observer.Observer} class.
 */
public class TimerObserver extends Observer {

	private final String metricId = "TimerObserver";
	private Metric<Long> metric;

	public TimerObserver() {
	}

	private TimerObserver(String id, InvokerInterface invoker) {
		super(id, invoker);
	}

	/**
	 * Creates a copy of the TimerObserver.
	 *
	 * @param functionId The unique identifier for the TimerObserver.
	 * @param invoker 	 The invoker to be observed.
	 * @return A new TimerObserver instance.
	 */
	@Override
	public TimerObserver copy(String id, InvokerInterface invoker) {
		return new TimerObserver(id, invoker);
	}

	/**
	 * Captures the start time before the execution of the observed function.
	 */
	@Override
	public void execute() {
		metric = new Metric<Long>(functionId, System.nanoTime());
	}

	/**
	 * Updates the captured metric with the total execution time and stores it in the metric collection.
	 */
	@Override
	public void update() {
		Long time = metric.getDataType();
		time = System.nanoTime() - time;
		metric.setDataType(time);

		MetricCollection.instantiate().addMetric(metricId, metric);
	}

	/**
	 * Calculates the maximum execution time of the observed function.
	 *
	 * @param functionId The identifier of the observed function.
	 * @return The maximum execution time, or null if no metrics are available.
	 */
	public Long calculateMaxTime(String functionId) {
		try {
			List<Long> list = MetricCollection.instantiate().getList(metricId, functionId);
			return calculateMaxMetric(list, Comparator.comparingLong(value -> value));
		}
		catch (NoResultAvailable e) {
			return null;
		}
	}

	/**
	 * Calculates the minimum execution time of the observed function.
	 *
	 * @param functionId The identifier of the observed function.
	 * @return The minimum execution time, or null if no metrics are available.
	 */
	public Long calculateMinTime(String functionId) {
		try {
			List<Long> list = MetricCollection.instantiate().getList(metricId, functionId);
			return calculateMinMetric(list, Comparator.comparingLong(value -> value));
		}
		catch (NoResultAvailable e) {
			return null;
		}
	}

	/**
	 * Calculates the average execution time of the observed function.
	 *
	 * @param functionId The identifier of the observed function.
	 * @return The average execution time, or null if no metrics are available.
	 */
	public Long calculateAverageTime(String functionId) {
		try {
			List<Long> list = MetricCollection.instantiate().getList(metricId, functionId);
			return (long) calculateAverageMetric(list, (var) -> var);
		}
		catch (NoResultAvailable e) {
			return null;
		}
	}

	/**
	 * Calculates the total execution time of the observed function.
	 *
	 * @param functionId The identifier of the observed function.
	 * @return The total execution time, or null if no metrics are available.
	 */
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
