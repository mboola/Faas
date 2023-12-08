package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetricSet {

	private Map<String, Map<String, List<Metric<Object>>>> dataCollected;

	public MetricSet()
	{
		dataCollected = new HashMap<String, Map<String, List<Metric<Object>>>>();
	}

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
