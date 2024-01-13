package policymanager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

/**
 * Implementation of the {@link PolicyManager} interface that distributes invocations in groups
 * that may vary in size among invokers.
 * <p>
 * This policy manager also implements the Java {@link Serializable} interface.
 */
public class BigGroup implements PolicyManager, Serializable {

	private long groupSize;
	private long invocationsDistributed;

	/** Index of last invoker selected from the list */
	private int	lastInvokerAssigned;
	/** Flag to check if a invoker that is full should be ignored when selecting invokers to assgin */
	private boolean ignoreFull;

	/**
	 * Initializes a new instance of the BigGroup class.
	 */
	public BigGroup() {
		lastInvokerAssigned = 0;
		ignoreFull = false;
		groupSize = 1;
		invocationsDistributed = 0;
	}

	/**
	 * Distributes the charge in single invocations 
	 * 
	 * @param invokers	The list of invokers.
	 * @param ram		The amount of required RAM for the invocation.
	 * @throws NoInvokerAvailable If no invoker has enough max ram to execute the function or there are no Invokers in the list.
	 */
	private void setSingleValues(List<InvokerInterface> invokers, long ram) throws NoInvokerAvailable {
		// Calculate the number of invokers with sufficient max RAM
		long invokersMaxRamUsable = invokers.stream()
				.filter(value -> {
					try {
						return value.getMaxRam() >= ram;
					} catch (RemoteException e) {
						return false;
					}
				})
				.count();
		// Throw an exception if no invoker has at least the specified max RAM
		if (invokersMaxRamUsable == 0)
			throw new NoInvokerAvailable("No Invoker Avaiable with at least " + ram + " RAM.");

		ignoreFull = false;
		groupSize = 1;
		invocationsDistributed = 0;
	}

	/**
	 * Sets values for distributing the charge in grouped invocations based on specified criteria.
	 * 
	 * @param invokers The list of invokers to consider.
	 * @param numInvocations The number of invocations to be distributed.
	 * @param ram The amount of required RAM for each invocation.
	 * @throws NoInvokerAvailable If no invoker has enough max RAM to execute the function or if there are no invokers in the list.
	 */
	private void setGroupValues(List<InvokerInterface> invokers, int numInvocations, long ram)
			throws NoInvokerAvailable {
		// Calculate the number of invokers with sufficient max RAM
		long invokersMaxRamUsable = invokers.stream()
				.filter(value -> {
					try {
						return value.getMaxRam() >= ram;
					} catch (RemoteException e) {
						return false;
					}
				})
				.count();

		// Throw an exception if no invoker has at least the specified max RAM
		if (invokersMaxRamUsable == 0)
			throw new NoInvokerAvailable("No Invoker Avaiable with at least " + ram + " RAM.");
		
		//we get the invoker with less ram available
		long lessAvailableRam = invokers.stream()
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
						return Long.MAX_VALUE;
					}
				})
				.min()
				.orElse(0);

		// Calculate the total available RAM
		long totalRamAvailable = invokers.stream()
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
		
		
		// Determine whether to ignore full invokers if charge can be distributed
		// between the available invokers and their ram
		if (totalRamAvailable < numInvocations * ram) {
			//if there is not enough ram we separate the charge in an uniform way
			ignoreFull = false;
			groupSize = (long) Math.ceil((double) numInvocations / invokersMaxRamUsable);
		} else {
			ignoreFull = true;
			groupSize = lessAvailableRam / ram;
		}
		invocationsDistributed = 0;
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len)
			return (pos + 1);
		else
			return (0);
	}

	/**
	 * Creates a new instance of the {@code BigGroup} policy manager.
	 * 
	 * @return A new {@code BigGroup} policy manager.
	 */
	@Override
	public PolicyManager copy() {
		return (new BigGroup());
	}

	/**
	 * Prepares the distribution of invocations among the provided list of invokers based on the specified parameters.
	 *
	 * @param invokers           The list of invokers.
	 * @param size               The number of invocations to be distributed.
	 * @param ram                The RAM requirement for the invocations.
	 * @throws NoInvokerAvailable If no invokers can execute the function.
	 * @throws RemoteException    If a remote communication error occurs.
	 */
	@Override
	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram)
			throws NoInvokerAvailable, RemoteException
	{
		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		if (size == 1) {
			setSingleValues(invokers, ram);
		} else if (!(size == 1)) {
			setGroupValues(invokers, size, ram);
		} else {   //in case we try to execute a function that cannot be executed
			setSingleValues(invokers, ram);
		}

		Logger logger = Logger.getLogger(PolicyManager.class.getName());
		
		for (InvokerInterface invoker : invokers) {
			try {
				invoker.setDistributionPolicyManager((int) groupSize, ram);
			} catch (NoInvokerAvailable e) {
				logger.warning("NoInvokerAvailable: Distribution could not be set to: " + invoker.toString());
			}
		}
	}

	/**
	 * Retrieves an invoker from the provided list based on the specified RAM requirement
	 * using the GroupSize set by {@link BigGroup#prepareDistribution}.
	 *
	 * @param invokers The list of available invokers.
	 * @param ram      The RAM requirement for the invoker selection.
	 * @return An invoker selected based on the RAM requirement.
	 * @throws NoPolicyManagerRegistered If the policy manager is not registered.
	 * @throws NoInvokerAvailable       If no invokers are available for selection.
	 * @throws RemoteException          If a remote communication error occurs.
	 */
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
		invoker = invokers.get(lastInvokerAssigned).selectInvoker(ram);
		invocationsDistributed++;
		return (invoker);
	}

}
