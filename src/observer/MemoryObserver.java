package observer;

import java.rmi.RemoteException;

import core.metrics.Metric;
import core.metrics.MetricCollection;

/**
 * Observer implementation for monitoring memory usage before it starts executing, 
 * storing the ram the invoker is using after reserving the ram used by the invocation.
 * <p>
 * This observer extends the {@link observer.Observer} class.
 */
public class MemoryObserver extends Observer {

	private final String	metricId	= "MemoryObserver";
	private Metric<Long>	metric;

	/**
	 * Executes the memory observation by capturing the RAM being used 
	 * and updating the MetricCollection with the observed memory metric.
	 */
	@Override
	public void execution() {
		try {
			metric = new Metric<Long>(functionId, invoker.getUsedRam());
			MetricCollection.instantiate().addMetric(metricId, metric);
		}
		catch (RemoteException e) {
			metric = null;
		}
	}

	/**
	 * Creates a copy of the MemoryObserver.
	 *
	 * @return A new instance of MemoryObserver.
	 */
	@Override
	public MemoryObserver copy() {
		return new MemoryObserver();
	}

}
