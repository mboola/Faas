package application;

public class TimerObserver implements Observer{

	private Controller	controller;

	@Override
	public <T> Metric<T> initialize(String id, Controller controller) {
		this.controller = controller;
		return (Metric<T>) (new Metric<Long>(id, System.nanoTime()));
	}

	@Override
	public <T> void update(Metric<T> metric) {
		Metric<Long> timeMetric = (Metric<Long>)metric;
		Long time = timeMetric.getDataType();
		time = System.nanoTime() - time;
		timeMetric.setDataType(time);
		//TODO change the way I access this
		controller.metrics.addTime(timeMetric);
	}

}
