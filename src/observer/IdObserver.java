package observer;

import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoResultAvailable;
import core.metrics.Metric;
import core.metrics.MetricCollection;

/**
 * An observer that collects and calculates metrics related to invocations.
 * Do not confuse this decorator with {@link observer.InvocationObserver} class. This stores the id of the invoker
 * to invoke a function the moment it will invoke a function.
 * <p>
 * This observer extends the {@link observer.Observer} class.
 */
public class IdObserver extends Observer {

	private final String	metricId = "IdObserver";
	private Metric<String>	metric;

	/**
     * Executes before the function is invoked. Initializes the metric with the function and invoker IDs.
     */
	@Override
	public void execution()
	{
		try {
			metric = new Metric<String>(functionId, invoker.getId());
		}
		catch (RemoteException e) {
			metric = null;
		}
	}

	/**
     * Updates the metric collection with the recorded metric if available.
     */
	@Override
	public void update() {
		if (metric != null)
			MetricCollection.instantiate().addMetric(metricId, metric);
	}

	/**
     * Creates a copy of the IdObserver.
     *
     * @return A new instance of IdObserver.
     */
	@Override
	public IdObserver copy() {
		return new IdObserver();
	}

	/**
     * Calculates the total times a specific invoker ID has been selected to invoke.
     *
     * @param functionId The ID of the function.
     * @param invokerID  The ID of the invoker.
     * @return The total count of times the invoker ID has been selected to invoke the function with the ID.
     */
	public Long calculateAllTimesInvoked(String functionId, String invokerID) {
		try {
			List<String> list = MetricCollection.instantiate().getList(metricId, functionId);
			return list.stream().filter((value) -> value.equals(invokerID)).count();
		}
		catch (NoResultAvailable e) {
			return 0L;
		}
	}

}
