package observer;

import java.rmi.RemoteException;

import core.metrics.Metric;
import core.metrics.MetricSet;

public class MemoryObserver extends Observer {

	private final String		metricId	= "MemoryObserver";
	private Metric<Long>		metric;

	@Override
	public void execution() {
		try {
			metric = new Metric<Long>(id, invoker.getUsedRam());
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
	public MemoryObserver copy() {
		return new MemoryObserver();
	}

}
