package policymanager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

/**
 * Implementation of the {@link PolicyManager} interface that distributes invocations uniformly among invokers.
 * <p>
 * This policy manager also implements the Java {@link Serializable} interface.
 */
public class UniformGroup implements PolicyManager, Serializable {

	private long groupSize;
	private long invocationsDistributed;

	/** Index of last invoker selected from the list */
	private int	lastInvokerAssigned;
	/** Flag to check if a invoker that is full should be ignored when selecting invokers to assgin */
	private boolean ignoreFull;

	/**
	 * Initializes a new instance of the UniformGroup class.
	 */
	public UniformGroup() {
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
	private void setSingleUniformValues(List<InvokerInterface> invokers, long ram) throws NoInvokerAvailable {
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
	private void setGroupUniformValues(List<InvokerInterface> invokers, int numInvocations, long ram)
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

		// Calculate the number of invokers with sufficient available RAM
		long invokersRamAvailable = invokers.stream()
				.filter(value -> {
					try {
						return value.getAvailableRam() >= ram;
					} catch (RemoteException e) {
						return false;
					}
				})
				.count();

		// Calculate the average available RAM among invokers with sufficient available RAM
		double averageRamAvailable = invokers.stream()
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
		
		// Determine whether to ignore full invokers if charge can be distributed
		// between the available invokers and their ram
		if (averageRamAvailable * invokersRamAvailable >= numInvocations * ram) {
			//if there is not enough ram we separate the charge in an uniform way
			ignoreFull = true;
			groupSize =  (long) Math.ceil((double) numInvocations / invokersRamAvailable);
		} else {
			ignoreFull = false;
			groupSize =  (long) Math.ceil((double) numInvocations / invokersMaxRamUsable);
		}
		//we do Math.ceil because at least group must be of one invocation**
		invocationsDistributed = 0;
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len) {
			return (pos + 1);
		} else {
			return (0);
		}
	}

	/**
	 * Creates a new instance of the {@code UniformGroup} policy manager.
	 * 
	 * @return A new {@code UniformGroup} policy manager.
	 */
	@Override
	public PolicyManager copy() {
		return new UniformGroup();
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
	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram)
			throws NoInvokerAvailable, RemoteException
	{
		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		if (size == 1) {
			setSingleUniformValues(invokers, ram);
		} else if (!(size == 1)) {
			setGroupUniformValues(invokers, size, ram);
		} else { //in case we try to execute a function that cannot be executed
			setSingleUniformValues(invokers, ram);
		}
		
		//TODO try catch? NoInvokerAvailable
		for (InvokerInterface invoker : invokers) {
			invoker.setDistributionPolicyManager((int) groupSize, ram);
		}
	}

	/**
	 * Retrieves an invoker from the provided list based on the specified RAM requirement
	 * using the GroupSize set by {@link UniformGroup#prepareDistribution}.
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

		// if the first time we select an invoker the pointer is pointing to one that cannot invoke
		// or we have reached the end of this group size
		if (invocationsDistributed == groupSize || invokers.get(lastInvokerAssigned).getMaxRam() < ram)
		{
			found = false;
			while (!found)
			{
				lastInvokerAssigned = updatePos(lastInvokerAssigned, invokers.size() - 1);
				if (invokers.get(lastInvokerAssigned).getMaxRam() >= ram) {
					if (!ignoreFull) {
						found = true;
					} else if (invokers.get(lastInvokerAssigned).getAvailableRam() >= ram) {
						found = true;
					}
				}
			}
			invocationsDistributed = 0;
		}
		
		invoker = invokers.get(lastInvokerAssigned).selectInvoker(ram);
		invocationsDistributed++;
		return (invoker);
	}

}
