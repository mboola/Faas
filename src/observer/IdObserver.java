package observer;

import java.rmi.RemoteException;

import core.metrics.Metric;
import core.metrics.MetricSet;

public class IdObserver extends Observer {

	private final String		metricId	= "IdObserver";
	private Metric<String>		metric;

	@Override
	public void execution()
	{
		try {
			metric = new Metric<String>(id, invoker.getId());
		}
		catch (RemoteException e) {
			metric = null;
		}
	}

	@Override
	public void update() {
		if (metric != null)
			MetricSet.instantiate().addMetric(metricId, metric);
	}

	@Override
	public IdObserver copy() {
		return new IdObserver();
	}

}
