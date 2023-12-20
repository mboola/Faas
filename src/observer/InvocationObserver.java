package observer;

import java.rmi.RemoteException;

import invoker.InvokerInterface;
import metrics.Metric;
import metrics.MetricSet;

public class InvocationObserver implements Observer {

	private String	metricId = "InvocationObserver";

	public <T> void initialize(String id, InvokerInterface invoker) throws Exception
	{
		try {
			MetricSet.instantiate().addMetric(metricId, new Metric<String>(id, invoker.getId()));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public <T> Metric<T> execution(String id, InvokerInterface invoker) {
		return (null);
	}

	@Override
	public <T> void update(Metric<T> metric) {
	}

}
