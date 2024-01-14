package observer;

import core.invoker.InvokerInterface;

/**
 * An interface defining the contract for creating observers that monitor the behavior of an {@link InvokerInterface}.
 * Observers can capture and analyze metrics associated with the invoker's execution.
 */
public interface ObserverInterface {

	/**
	 * Creates a copy of the observer.
	 * This method is useful for creating multiple instances of an observer with the same configuration.
	 *
	 * @param functionId The unique identifier for the observer.
	 * @param invoker 	 The invoker to be observed.
	 * @return A new instance of the observer.
	 */
	public Observer copy(String functionId, InvokerInterface invoker);
	
	/**
	 * Method to be implemented by subclasses for any post-execution actions.
	 * Initializes the observer with a unique identifier and the associated invoker.
	 * This method is called when the invoker gets asigned by the PolicyManager
	 */
	public void initialize();

	/**
	 * Method to be implemented by subclasses for any pre-execution actions.
	 * This method is called before the associated function is executed by the invoker.
	 */
	public void execution();

	/**
	 * Method to be implemented by subclasses for any post-execution actions.
	 * This method is called after the associated function is executed by the invoker.
	 */
	public void update();
	
}
