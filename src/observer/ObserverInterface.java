package observer;

import core.invoker.InvokerInterface;

/**
 * An interface defining the contract for creating observers that monitor the behavior of an {@link InvokerInterface}.
 * Observers can capture and analyze metrics associated with the invoker's execution.
 */
public interface ObserverInterface {

	/*
	 * Called when assigned.
	 */
	/**
     * Initializes the observer with a unique identifier and the associated invoker.
     * This method is typically called when the observer is assigned to an invoker.
     *
     * @param id      The unique identifier for the observer.
     * @param invoker The invoker to be observed.
     */
	public void initialize(String id, InvokerInterface invoker);

	/*
	 * Called before execution.
	 */
	public void execution();

	/*
	 * Called after execution.
	 */
	public void update();

	public Observer copy();
	
}
