package policymanager;

import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

/**
 * Implementation of the PolicyManager interface using a uniform distribution
 * of function assignments among Invokers.
 */
public class UniformGroup implements PolicyManager{

	private boolean executingGroup;

	private long groupSize;
	private long invocationsDistributed;

	private int	lastInvokerAssigned;
	private boolean ignoreFull;

	//values to distribute the charge in single invocations without knowing the ram it will consume
	private void setSingleUniformValues(List<InvokerInterface> invokers, long ram) throws NoInvokerAvailable
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

	private void setGroupUniformValues(List<InvokerInterface> invokers, int numInvocations, long ram) throws NoInvokerAvailable
	{
		//get the average of invokers ram available, num of invokers, and how may invokers can execute the function
		long    invokersMaxRamUsable;
		long    invokersRamAvailable;
		double  averageRamAvailable;
		
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

		//and here I get the invokers that have available ram
		invokersRamAvailable = invokers.stream()
			.filter(value -> {
				try {
					return value.getAvailableRam() >= ram;
				} catch (RemoteException e) {
					return false;
				}
			})
			.count();

		//and here I get the average of those who have available ram
		averageRamAvailable = invokers.stream()
			.filter(value -> {
				try {
					return value.getAvailableRam() >= ram;
				} catch (RemoteException e) {
					return false;
				}
			})
			.mapToDouble(value -> {
				try {
					return value.getAvailableRam();
				} catch (RemoteException e) {
					return 0;
				}
			})
			.average()
			.orElse(0.0);
		
		//if the charge can be distributed between the available invokers and their ram
		if (averageRamAvailable * invokersRamAvailable >= numInvocations * ram)
		{
			//we will distribute the charge to only available ones.
			ignoreFull = true;
			groupSize =  (long) Math.ceil((double) numInvocations / invokersRamAvailable);
		}
		else
		{
			//if it cannot be distributed to only available ones, we distribute it
			//to all of them (that can execute it)
			ignoreFull = false;
			groupSize =  (long) Math.ceil((double) numInvocations / invokersMaxRamUsable);
		}
		//we do Math.ceil because at least group must be one
		invocationsDistributed = 0;
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len)
			return (pos + 1);
		else
			return (0);
	}

	public UniformGroup() {
		lastInvokerAssigned = 0;
		executingGroup = false;
		ignoreFull = false;
		groupSize = 1;
		invocationsDistributed = 0;
	}

	@Override
	public PolicyManager copy() {
		return (new UniformGroup());
	}

	//TODO: see executingGroup
	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram, boolean singleInvocation)
			throws NoInvokerAvailable, RemoteException
	{
		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		//do we need to prepare the group in the most efficient distribution possible?
		if (singleInvocation && executingGroup)
		{
			executingGroup = false;
			setSingleUniformValues(invokers, ram);
		}
		else if (!singleInvocation)
		{
			executingGroup = true;
			setGroupUniformValues(invokers, size, ram);
		}
		else    //in case we want to execute a function that cannot be executed
			setSingleUniformValues(invokers, ram);
		
		for (InvokerInterface invoker : invokers) {
			invoker.setDistributionPolicyManager((int) groupSize, ram, singleInvocation);
		}
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
		//in theory this should never throw a NoInvokerAvailable.
		invoker = invokers.get(lastInvokerAssigned).selectInvoker(ram);
		//System.out.println(invoker.getId());
		invocationsDistributed++;
		return (invoker);
	}

}
