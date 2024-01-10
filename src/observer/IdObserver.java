package observer;

import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoResultAvailable;
import core.metrics.Metric;
import core.metrics.MetricCollection;

public class IdObserver extends Observer {

	private final String		metricId = "IdObserver";
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
			MetricCollection.instantiate().addMetric(metricId, metric);
	}

	@Override
	public IdObserver copy() {
		return new IdObserver();
	}

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
