package policymanager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

/**
 * A simple implementation of the Round Robin policy for selecting invokers from a list.
 * This policy selects invokers in a circular order, distributing invocations among them.
 * <p>
 * This policy manager implements the {@link policymanager.PolicyManager} interface and Java {@link Serializable} interface.
 */
public class RoundRobin implements PolicyManager, Serializable {

	/** Index of last invoker selected from the list */
	private int	lastInvokerAssigned;

	/**
	 * Constructs a RoundRobin instance with initial state.
	 */
	public RoundRobin () {
		lastInvokerAssigned = 0;
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len)
			return (pos + 1);
		else
			return (0);
	}

	/**
	 * This policy will select uniformly the invokers that has resources availables. If one invoker
	 * is full, it will jumpt to the next one.
	 * If none of them have resources, they will get again selected uniformly.
	 *
	 * @param invokers The list of invokers.
	 * @param ram      The amount of required RAM for the invocation.
	 * @return The selected invoker.
	 * @throws NoPolicyManagerRegistered If the invoker selected is a composite and his policy manager is not properly registered.
	 * @throws NoInvokerAvailable        If no invoker has enough max ram to execute the function or there are no Invokers in the list.
	 * @throws RemoteException           If a remote exception occurs.
	 */
	@Override
	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException
	{
		InvokerInterface invoker;
		InvokerInterface invokerExecutable;
		int	invokerExecutablePos = 0;
		int	lastInvokerChecked;
		int	len;

		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		lastInvokerChecked = lastInvokerAssigned;
		len = invokers.size() - 1;
		lastInvokerChecked = updatePos(lastInvokerChecked, len);
		invoker = null;
		invokerExecutable = null;
		while (lastInvokerAssigned != lastInvokerChecked) {
			// we select a invoker from the list and check if it can execute the function
			// or, in case it is a composite invoker, any invoker from his list can invoke it.
			try {
				invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
				if (invokerExecutable == null)
				{
					invokerExecutable = invoker;
					invokerExecutablePos = lastInvokerChecked;
				}
				// if the invoker selected has enough ram we end the loop
				if (invoker.getAvailableRam() - ram >= 0)
					break ;
				lastInvokerChecked = updatePos(lastInvokerChecked, len);
			}
			catch (NoInvokerAvailable e) {
				lastInvokerChecked = updatePos(lastInvokerChecked, len);
			}
		}
		// if a invoker with enough ram was found
		if (lastInvokerAssigned != lastInvokerChecked)
			lastInvokerAssigned = lastInvokerChecked;
		// if at least one invoker can execute the function but it is full
		else if (lastInvokerAssigned == lastInvokerChecked && invokerExecutable != null)
		{
			invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
			if (invoker.getAvailableRam() - ram >= 0)
				return (invoker);
			lastInvokerAssigned = invokerExecutablePos;
			return (invokerExecutable);
		}
		// this will throw NoInvokerAvailable exception.
		else
			invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
		return (invoker);
	}

	/**
	 * Creates a new instance of the {@code RoundRobin} policy manager.
	 * 
	 * @return A new {@code RoundRobin} policy manager.
	 */
	@Override
	public PolicyManager copy() {
		return (new RoundRobin());
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram)
			throws NoInvokerAvailable, RemoteException {
	}
	
}
