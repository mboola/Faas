public class TimerObserver implements Observer{

	@Override
	public <T, R> void update(Metric<T, R> metric) {
		metric.updateTime(System.currentTimeMillis());
	}

}
