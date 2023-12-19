package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetricSet {

	private Map<String, Map<String, List<Metric<Object>>>> dataCollected;

	private static volatile MetricSet uniqueInstance = null;
	private static Object mutex = new Object();

	/**
	 * Checks if the MetricSet is instanciated, creates one if it isn't.
	 * This method is thread-safe.
	 * 
	 * @return The Singleton instance of MetricSet. 
	 */
	public static MetricSet instantiate() {
		MetricSet instance;

		instance = uniqueInstance;
		if (uniqueInstance == null)
		{
			synchronized (mutex)
			{
				instance = uniqueInstance;
				if (instance == null)
					instance = uniqueInstance = new MetricSet();
			}
		}
		return (instance);
	}

	/**
	 * Constructs a new instance of MetricSet and instantiates all the structs it uses.
	 */
	private MetricSet() {
		dataCollected = new HashMap<String, Map<String, List<Metric<Object>>>>();
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
			str += metric.getDataStr();
		}
		return (str);
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

}
