package core.metrics;

import java.util.LinkedList;
import java.util.List;

import core.invoker.InvokerInterface;
import observer.Observer;

/**
 * A class responsible for collecting and managing metrics using observers.
 * The metrics are associated with specific observers and are collected through an invoker.
 */
public class MetricRecollector {
    
	private List<Observer> observers;

	/**
     * This constructor initializes the MetricRecollector with a list of Observers 
	 * from MetricCollection, performing a deep copy with the given function ID
     * and the invoker responsible for invoking the function.
     *
     * @param id      the identifier for the MetricRecollector, representing a function to collect metrics from
     * @param invoker the invoker to be observed for metric collection
     */
	public MetricRecollector(String id, InvokerInterface invoker) {

		List<Observer> obs = MetricCollection.instantiate().getObservers();

		// I create a deep copy
		observers = new LinkedList<>();
		for (Observer observer : obs) {
			observers.add(observer.copy(id, invoker));
		}
	}

	/**
     * Called when the function gets assigned to an invoker.
     */
	public void initializeObservers() 
	{
		for (Observer observer : observers) {
			observer.initialize();
		}
	}

	/**
     * Called before the invoker executes the function. This doesn't necessarily mean that it will be called after initializeObservers.
     * If the invoker is full, there may be a waiting time until the invoker has space to execute.
     * Executes all registered observers, allowing them to perform actions before the function execution.
     */
	public void executeObservers()
	{
		for (Observer observer : observers) {
			observer.execution();
		}
	}

	/**
     * Called after the invoker ends the execution.
     * Notifies all registered observers about the completion of the execution.
     */
	public void notifyObservers()
	{
		for (Observer observer : observers) {
			observer.update();
		}
	}

}
