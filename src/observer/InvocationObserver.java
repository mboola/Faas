package observer;

import java.rmi.RemoteException;

import core.invoker.InvokerInterface;
import core.metrics.Metric;
import core.metrics.MetricCollection;

/**
 * An observer that initializes a metric associated with invocations upon initialization.
 * Do not confuse this decorator with {@link observer.IdObserver} class. This stores the id of the invoker
 * to invoke a function the moment it gets selected by the PolicyManager.
 * <p>
 * This observer extends the {@link observer.Observer} class.
 * 
 * @see policymanager.PolicyManager
 */
public class InvocationObserver extends Observer {

	private final String metricId = "InvocationObserver";

	public InvocationObserver() {
	}

	private InvocationObserver(String id, InvokerInterface invoker) {
		super(id, invoker);
	}

	/**
	 * Creates a copy of the InvocationObserver.
	 *
	 * @param functionId The unique identifier for the InvocationObserver.
	 * @param invoker 	 The invoker to be observed.
	 * @return A new InvocationObserver instance.
	 */
	@Override
	public InvocationObserver copy(String id, InvokerInterface invoker) {
		return new InvocationObserver(id, invoker);
	}

	/**
	 * Initializes the observer with the provided ID and invoker, adding a metric to the collection.
	 *
	 * @param id      The ID of the observer.
	 * @param invoker The invoker we will be observing.
	 */
	@Override
	public void initialize() {
		try {
			MetricCollection.instantiate().addMetric(metricId, new Metric<String>(functionId, invoker.getId()));
		} catch (RemoteException e) {
		}
	}

}
