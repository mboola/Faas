package policymanager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

public class RoundRobin implements PolicyManager, Serializable {

	private int	lastInvokerAssigned;

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
	 */
	@Override
	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException
	{
		InvokerInterface invoker;
		InvokerInterface invokerExecutable;
		int	invokerExecutablePos = 0;
		int	lastInvokerChecked;
		int	len;

		//if there are no invokers we cant select them
		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		lastInvokerChecked = lastInvokerAssigned;
		len = invokers.size() - 1;
		lastInvokerChecked = updatePos(lastInvokerChecked, len);
		invoker = null;
		invokerExecutable = null;
		while (lastInvokerAssigned != lastInvokerChecked) {
			//this selects an invoker from this invoker that can execute the invokable
			try {
				invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
				//check if it has resources to run
				if (invokerExecutable == null)
				{
					invokerExecutable = invoker;
					invokerExecutablePos = lastInvokerChecked;
				}
				if (invoker.getAvailableRam() - ram >= 0)
					break ;
				lastInvokerChecked = updatePos(lastInvokerChecked, len);
			}
			catch (NoInvokerAvailable e) {
				lastInvokerChecked = updatePos(lastInvokerChecked, len);
			}
		}
		if (lastInvokerAssigned != lastInvokerChecked)
			lastInvokerAssigned = lastInvokerChecked;
		//if more than one invoker but all full
		else if (lastInvokerAssigned == lastInvokerChecked && invokerExecutable != null)
		{
			//check if the invoker I started poiting to has enough resources
			invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
			if (invoker.getAvailableRam() - ram >= 0)
				return (invoker);
			lastInvokerAssigned = invokerExecutablePos;
			return (invokerExecutable);
		}
		//case only one invoker
		else
			invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
		return (invoker);
	}

	@Override
	public PolicyManager copy() {
		return (new RoundRobin());
	}

	@Override
	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram, boolean singleInvocation)
			throws NoInvokerAvailable, RemoteException
	{
	}
	
}
