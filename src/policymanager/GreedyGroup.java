package policymanager;

import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

public class GreedyGroup implements PolicyManager{

	int	lastInvokerAssigned;

	public GreedyGroup () {
		lastInvokerAssigned = 0;
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len)
			return (pos + 1);
		else
			return (0);
	}

	private	InvokerInterface getNextInvokerList(List<InvokerInterface> invokers, long ram) throws NoPolicyManagerRegistered, RemoteException
	{
		InvokerInterface invokerSelected;
		InvokerInterface currInvoker;
		boolean	found;

		found = false;
		lastInvokerAssigned = updatePos(lastInvokerAssigned, invokers.size() - 1);
		currInvoker = invokers.get(lastInvokerAssigned);
		invokerSelected = null;
		while (!found) {
			try {
				invokerSelected = currInvoker.selectInvoker(ram);
				found = true;
			}
			catch (NoInvokerAvailable e) {
				lastInvokerAssigned = updatePos(lastInvokerAssigned, invokers.size() - 1);
				currInvoker = invokers.get(lastInvokerAssigned);
			}
		}
		return (invokerSelected);
	}

	@Override
	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException
	{
		InvokerInterface invokerSelected;
		InvokerInterface lastInvokerSelected;
		int		hasEnoughRam;
		long	invokerRam;
		long	lessRam;

		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		//here we store the invoker with less ram available that can execute the function
		lastInvokerSelected = null;
		//if there is an invoker that can execute the function
		hasEnoughRam = 0;
		lessRam = Long.MAX_VALUE;
		for (InvokerInterface invoker : invokers) {
			try {
				//we select a invoker from all the invokers this (maybe) composite has
				invokerSelected = invoker.selectInvoker(ram);
				//if we are here it means the invoker selected has enough max ram to execute the function
				hasEnoughRam = 1;
				//but has ram available?
				invokerRam = invoker.getAvailableRam();
				if (invokerRam >= ram && invokerRam < lessRam)
				{
					lessRam = invokerRam;
					lastInvokerSelected = invokerSelected;
				}
			}
			catch (NoInvokerAvailable e) {
				//we update the position
			}
		}
		//all invokers are full, distribute the invokers as a round robin
		if (lastInvokerSelected == null && hasEnoughRam == 1)
			return (getNextInvokerList(invokers, ram));
		if (lastInvokerSelected == null) throw new NoInvokerAvailable("No Invoker Avaiable with at least " + ram + " RAM.");
		return (lastInvokerSelected);
	}

	@Override
	public PolicyManager copy() {
		return (new GreedyGroup());
	}

	@Override
	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram, boolean singleInvocation)
            throws NoInvokerAvailable, RemoteException
	{
		
	}
		
}
