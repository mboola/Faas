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

	private final String	metricId = "InvocationObserver";

	/**
	 * Initializes the observer with the provided ID and invoker, adding a metric to the collection.
	 *
	 * @param id      The ID of the observer.
	 * @param invoker The invoker we will be observing.
	 */
	@Override
	public void initialize(String id, InvokerInterface invoker) {
		super.initialize(id, invoker);
		try {
			MetricCollection.instantiate().addMetric(metricId, new Metric<String>(id, invoker.getId()));
		} catch (RemoteException e) {
		}
	}

	/**
	 * Creates a copy of the InvocationObserver.
	 *
	 * @return A new instance of InvocationObserver.
	 */
	@Override
	public InvocationObserver copy() {
		return new InvocationObserver();
	}

}
