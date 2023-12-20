package metrics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import invoker.InvokerInterface;
import observer.Observer;

public class MetricRecollector {
    
	private HashMap<Observer, Metric<Object>> metrics;
	private InvokerInterface	invoker;
	private List<Observer>		observers;
	private String				id;

	/**
	 * Constructs a new instance of MetricsRecollector and instantiates all the structs it uses.
	 */
	public MetricRecollector(String id, InvokerInterface invoker) {
		this.id = id;
		this.invoker = invoker;

		metrics = new HashMap<Observer, Metric<Object>>();

		//here I copy this list of observers from MetricSet
		observers = new LinkedList<>(MetricSet.instantiate().getObservers());
	}

	/*
	 * This stores all the metrics necessary inmediatly after the Invoker gets selected by the PolicyManager.
	 * //TODO: maybe also initialize things
	 */
	public void initializeObservers() throws Exception 
	{
		for (Observer observer : observers) {
			observer.initialize(id, invoker);
		}
	}

	/**
	 * This 
	 * 
	 * @param id Id of the function. Needed by the observers to update the content of a dictionary in the controller.
	 * @return Map of metrics initialized. These metrics will be modified by 'notifyAllObservers' to create final metrics.
	 * @throws Exception
	 */
	public void executeObservers() throws Exception 
	{
		for (Observer observer : observers) {
			metrics.put(observer, observer.execution(id, invoker));
		}
	}

	/**
	 * This modifies all the values of the metrics created by 'initializeAllObservers'
	 * 
	 * @param metrics Map of all the metrics to be updated by the observers
	 * 
	 * <p><strong>Note:</strong> If the list of observers changed between initialization and this method,
	 * two things can happen:
	 * <ul>
	 * 		<li> If observers were added, the new observers will not be notified.</li>
	 * 		<li> If observers were removed, the removed observers will not be notified.</li>
	 * </ul>
	 * This is to ensure a correct funcionality of the observers.
	 * </p>
	 */
	public void notifyObservers()
	{
		for (Observer observer : observers) {
			observer.update(metrics.get(observer));
		}
	}
}
