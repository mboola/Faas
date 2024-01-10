package core.metrics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.exceptions.NoResultAvailable;
import core.exceptions.OperationNotValid;
import observer.Observer;

public class MetricCollection {

	private Map<String, Map<String, List<Metric<Object>>>> dataCollected;

	private List<Observer>				observers;

	private static volatile MetricCollection	uniqueInstance = null;
	private static Object				mutex = new Object();

	/**
	 * Checks if the MetricCollection is instanciated, creates one if it isn't.
	 * This method is thread-safe.
	 * 
	 * @return The Singleton instance of MetricCollection. 
	 */
	public static MetricCollection instantiate() {
		MetricCollection instance;

		instance = uniqueInstance;
		if (uniqueInstance == null)
		{
			synchronized (mutex)
			{
				instance = uniqueInstance;
				if (instance == null)
					instance = uniqueInstance = new MetricCollection();
			}
		}
		return (instance);
	}

	/**
	 * Constructs a new instance of MetricCollection and instantiates all the structs it uses.
	 */
	private MetricCollection() {
		dataCollected = new HashMap<String, Map<String, List<Metric<Object>>>>();
		observers = new LinkedList<Observer>();
	}

	/**
	 * Retrieves data associated with a specific metric and function identifier.
	 *
	 * This method looks up the provided metric ID in it's data collected, and if found, retrieves the data
	 * associated with the given  ID. The method concatenates the data from each metric associated with
	 * the specified function and returns the resulting string.
	 *
	 * @param metricId   The ID of the metric for which data is requested.
	 * @param functionId The ID of the function associated with the requested data.
	 * @return A string containing the concatenated data from each metric associated with the specified function.
	 *         An empty string is returned if the ID of the metric is not found or if no data is available.
	 */
	public String getData(String metricId, String functionId)
	{
		Map<String, List<Metric<Object>>> metricMap;
		String	str = "";
		
		metricMap = dataCollected.get(metricId);
		if (metricMap == null)
			return (str);
		for (Metric<Object> metric : metricMap.get(functionId)) {
			str += metric.getDataStr() + " ";
		}
		str = str.trim();
		return (str);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String metricId, String functionId) throws NoResultAvailable
	{
		Map<String, List<Metric<Object>>> metricMap;
		List<Metric<Object>> nonCastedMetrics;

		if (!dataCollected.containsKey(metricId)) throw new NoResultAvailable("No dictionary of metrics for that observer.");
		metricMap = dataCollected.get(metricId);

		if (!metricMap.containsKey(functionId)) throw new NoResultAvailable("No metrics stored with that function ID.");
		nonCastedMetrics = metricMap.get(functionId);

		List<T> castedMetrics = nonCastedMetrics.stream()
			.map(metric -> ((Metric<T>) metric).getDataType())
			.collect(Collectors.toList());

        return castedMetrics;
	}

	/**
	 * Method used from observers to inject a new metric in our metrics.
	 * 
	 * @param metricId Id of the metric to add.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> void addMetric(String metricId, Metric<T> metric)
	{
		Map<String, List<Metric<Object>>> metricMap;
		List<Metric<Object>> metrics;

		//if there isn't a hashmap with that id, we create it
		if (!dataCollected.containsKey(metricId))
			dataCollected.put(metricId, new HashMap<String, List<Metric<Object>>>());

		//we get that hashmap
		metricMap = dataCollected.get(metricId);

		//if that hashmap doesn't have a list of metrics with that function id, we create it
		if (!metricMap.containsKey(metric.getFunctionId()))
			metricMap.put(metric.getFunctionId(), new LinkedList<Metric<Object>>());
		
		//we get the list of metrics
		metrics = metricMap.get(metric.getFunctionId());
		metrics.add((Metric<Object>)metric);
	}

	/**
	 * Adds an observer to the list of observers to be used by all invokers.
	 * 
	 * @param observer Observer that will be notified when a function is invoked.
	 */
	public void addObserver(Observer observer) throws OperationNotValid
	{
		if (observer == null) throw new OperationNotValid("Observer cannot be null.");

		Class<?> classType = observer.getClass();
		for (Observer obs : observers) {
			if (classType.isInstance(obs))
			throw new OperationNotValid("Observer is already registered.");
		}
		observers.add(observer);
	}

	/**
	 * Removes an observer to the list of observers to be used by all invokers.
	 * 
	 * @param observer Observer that won't be notified when a function is invoked.
	 */
	public void removeObserver(Observer observer) throws OperationNotValid
	{
		if (observer == null) throw new OperationNotValid("Observer to delete cannot be null.");
		if (!observers.contains(observer)) throw new OperationNotValid("Observer is already registered.");
		observers.remove(observer);
	}

	public List<Observer> getObservers()
	{
		return observers;
	}

}
