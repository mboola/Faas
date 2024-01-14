package observer;

import java.rmi.RemoteException;

import core.invoker.InvokerInterface;
import core.metrics.Metric;
import core.metrics.MetricCollection;

/**
 * Observer implementation for monitoring memory usage before it starts executing, 
 * storing the ram the invoker is using after reserving the ram used by the invocation.
 * <p>
 * This observer extends the {@link observer.Observer} class.
 */
public class MemoryObserver extends Observer {

	private final String metricId = "MemoryObserver";
	private Metric<Long> metric;

	public MemoryObserver() {
	}

	private MemoryObserver(String id, InvokerInterface invoker) {
		super(id, invoker);
	}

	/**
	 * Creates a copy of the MemoryObserver.
	 *
	 * @param functionId The unique identifier for the MemoryObserver.
	 * @param invoker 	 The invoker to be observed.
	 * @return A new MemoryObserver instance.
	 */
	@Override
	public MemoryObserver copy(String id, InvokerInterface invoker) {
		return new MemoryObserver(id, invoker);
	}

	/**
	 * Executes the memory observation by capturing the RAM being used 
	 * and updating the MetricCollection with the observed memory metric.
	 */
	@Override
	public void execute() {
		try {
			metric = new Metric<Long>(functionId, invoker.getUsedRam());
			MetricCollection.instantiate().addMetric(metricId, metric);
		}
		catch (RemoteException e) {
			metric = null;
		}
	}

}
