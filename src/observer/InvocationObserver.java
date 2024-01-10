package observer;

import java.rmi.RemoteException;

import core.invoker.InvokerInterface;
import core.metrics.Metric;
import core.metrics.MetricSet;

public class InvocationObserver extends Observer {

	private final String	metricId = "InvocationObserver";

	@Override
	public void initialize(String id, InvokerInterface invoker) {
		super.initialize(id, invoker);
		try {
			MetricSet.instantiate().addMetric(metricId, new Metric<String>(id, invoker.getId()));
		} catch (RemoteException e) {
		}
	}

	@Override
	public InvocationObserver copy() {
		return new InvocationObserver();
	}

}
