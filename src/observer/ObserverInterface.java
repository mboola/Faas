package observer;

import core.invoker.InvokerInterface;

public interface ObserverInterface {

	/*
	 * Called when assigned.
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
