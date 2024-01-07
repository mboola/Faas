package policymanager;

import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

public class BigGroup implements PolicyManager {

	private boolean executingGroup;

	private long groupSize;
	private long invocationsDistributed;

	private int	lastInvokerAssigned;
	private boolean ignoreFull;

	private void setSingleValues(List<InvokerInterface> invokers, long ram) throws NoInvokerAvailable
	{
		long    invokersMaxRamUsable;

		//here I get the num of invokers that can execute the function
		invokersMaxRamUsable = invokers.stream()
			.filter(value -> {
				try {
					return value.getMaxRam() >= ram;
				} catch (RemoteException e) {
					return false;
				}
			})
			.count();
		//If not a single invoker can execute this function
		if (invokersMaxRamUsable == 0)
			throw new NoInvokerAvailable("No Invoker Avaiable with at least " + ram + " RAM.");

		ignoreFull = false;
		groupSize = 1;
		invocationsDistributed = 0;
	}

	private void setGroupValues(List<InvokerInterface> invokers, int numInvocations, long ram) throws NoInvokerAvailable
	{
		//get the average of invokers ram available, num of invokers, and how may invokers can execute the function
		long    invokersMaxRamUsable;
		long	lessAvailableRam;
		long 	totalRamAvailable;

		//get the invoker with less available ram, and define groups based on that invoker
		
		//here I get the num of invokers that can execute the function
		invokersMaxRamUsable = invokers.stream()
			.filter(value -> {
				try {
					return value.getMaxRam() >= ram;
				} catch (RemoteException e) {
					return false;
				}
			})
			.count();
		
		//If not a single invoker can execute this function
		if (invokersMaxRamUsable == 0)
			throw new NoInvokerAvailable("No Invoker Avaiable with at least " + ram + " RAM.");
		
		//we get the invoker with less ram available
		lessAvailableRam = invokers.stream()
			.filter(value -> {
				try {
					return value.getMaxRam() >= ram;
				} catch (RemoteException e) {
					return false;
				}
			})
			.mapToLong(invoker -> {
				try {
					return invoker.getAvailableRam();
				} catch (RemoteException e) {
					return Long.MAX_VALUE; // Handle the exception appropriately
				}
			})
			.min()
			.orElse(0);

		//and here I get the average of those who have available ram
		totalRamAvailable = invokers.stream()
			.filter(value -> {
				try {
					return value.getMaxRam() >= ram;
				} catch (RemoteException e) {
					return false;
				}
			})
			.mapToLong(value -> {
				try {
					return value.getAvailableRam();
				} catch (RemoteException e) {
					return 0;
				}
			})
			.sum();
		
		//if all invokers are being used (totalRamAvailable == 0) or
		//if not all invokers are being used but there is not enough available ram
		if (totalRamAvailable < numInvocations * ram)
		{
			//we separate the charge in an uniform way
			ignoreFull = false;
			groupSize = (long) Math.ceil((double) numInvocations / invokersMaxRamUsable);
		}
		//there is enough ram available to execute
		else
		{
			ignoreFull = true;
			groupSize = lessAvailableRam / ram;
		}
		invocationsDistributed = 0;
	}

	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram, boolean singleInvocation)
			throws NoInvokerAvailable, RemoteException
	{
		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		//do we need to prepare the group in the most efficient distribution possible?
		if (singleInvocation && executingGroup)
		{
			executingGroup = false;
			setSingleValues(invokers, ram);
		}
		else if (!singleInvocation)
		{
			executingGroup = true;
			setGroupValues(invokers, size, ram);
		}
		else    //in case we want to execute a function that cannot be executed
			setSingleValues(invokers, ram);
        
		for (InvokerInterface invoker : invokers) {
			invoker.setDistributionPolicyManager((int) groupSize, ram, singleInvocation);
		}
	}

	public BigGroup() {
		lastInvokerAssigned = 0;
		executingGroup = false;
		ignoreFull = false;
		groupSize = 1;
		invocationsDistributed = 0;
	}

	@Override
	public PolicyManager copy() {
		return (new BigGroup());
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len)
			return (pos + 1);
		else
			return (0);
	}

	@Override
	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram)
			throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException
	{
		InvokerInterface invoker;
		boolean found;

		//if the first time we select an invoker the pointer is pointing to one that cannot invoke
		if (invocationsDistributed == groupSize || invokers.get(lastInvokerAssigned).getMaxRam() < ram)
		{
			//do we need to assign another invoker or this has space for another group?
			if ( Math.ceil((double)invokers.get(lastInvokerAssigned).getAvailableRam() / ram ) >= groupSize)
				invocationsDistributed = 0;
			else
			{
				//here we find next invoker selected
				found = false;
				while (!found)
				{
					lastInvokerAssigned = updatePos(lastInvokerAssigned, invokers.size() - 1);
					if (invokers.get(lastInvokerAssigned).getMaxRam() >= ram)
					{
						if (!ignoreFull)
							found = true;
						else if (invokers.get(lastInvokerAssigned).getAvailableRam() >= ram)
							found = true;
					}
				}
				invocationsDistributed = 0;
			}
		}
		//in theory this should never throw a NoInvokerAvailable.
		invoker = invokers.get(lastInvokerAssigned).selectInvoker(ram);
		System.out.println(invoker.getId());
		invocationsDistributed++;
		return (invoker);
	}

}
