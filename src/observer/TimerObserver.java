package observer;

import application.Controller;
import application.Metric;
import invoker.InvokerInterface;

public class TimerObserver implements Observer {

	private Controller	controller;
	private String		metricId	= "TimerObserver";
	
	@Override
	public void preinitialize(String id, Controller controller, InvokerInterface invoker) {
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public <T> Metric<T> initialize(String id, Controller controller, InvokerInterface invoker)
	{
		this.controller = controller;
		return (Metric<T>) (new Metric<Long>(id, System.nanoTime()));
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> void update(Metric<T> metric) {
		Metric<Long> timeMetric = (Metric<Long>)metric;
		Long time = timeMetric.getDataType();
		time = System.nanoTime() - time;
		timeMetric.setDataType(time);

		//TODO change the way I access this
		controller.metrics.addMetric(metricId, timeMetric);
	}
	
}
