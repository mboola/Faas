public interface Observer {
	
	public <T> Metric<T> initialize(String id, Controller controller);

	public <T> void update(Metric<T> metric);

}
