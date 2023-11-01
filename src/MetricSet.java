import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetricSet {

	private Map<String, List<Metric<Long>>> timeCollected = new HashMap<String, List<Metric<Long>>>();
	
	public void addTime(Metric<Long> metric)
	{
		List<Metric<Long>> metrics;

		metrics = timeCollected.get(metric.getId());
		if (metrics == null)
		{
			metrics = new LinkedList<Metric<Long>>();
			timeCollected.put(metric.getId(), metrics);
		}
		metrics.add(metric);
	}

	public void showTime(String id)
	{
		for (Metric<Long> metric : timeCollected.get(id)) {
			System.out.println("" + metric.getDataType());
		}
	}

}
