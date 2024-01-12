package core.invoker;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.exceptions.OperationNotValid;
import policymanager.PolicyManager;

/**
 * A CompositeInvoker is a specialized type of Invoker that can manage a list of child Invokers.
 * It implements the InvokerInterface and is responsible for selecting the appropriate child Invoker
 * based on the configured policy of the policy manager and available resources.
 */
public class CompositeInvoker extends Invoker {

	/** The list of child Invokers managed by this CompositeInvoker. */
	private List<InvokerInterface>	invokers;

	/** The PolicyManager responsible for defining the distribution policy for function execution. */
	private	PolicyManager			policyManager;

	/**
     * Private constructor for CompositeInvoker.
     *
     * @param ram     The maximum RAM for the CompositeInvoker.
     * @param threads The number of threads to be used by the CompositeInvoker.
     */
	private CompositeInvoker(long ram, int threads) {
		super(ram, threads);
		invokers = new LinkedList<InvokerInterface>();
	}

	/**
     * Creates a new instance of CompositeInvoker with the specified maximum RAM and thread count.
     *
     * @param ram     The maximum RAM for the CompositeInvoker.
     * @param threads The number of threads to be used by the CompositeInvoker.
     * @return A new CompositeInvoker instance, or null if invalid parameters are provided.
     */
	public static CompositeInvoker createInvoker(long ram, int threads) {
		if (ram <= 0 || threads <= 0)
			return (null);
		else
			return (new CompositeInvoker(ram, threads));
	}

	/**
     * Selects a Invoker to execute a function based on the RAM it consumes and the configured policy. If the Invoker selected
	 * has not enough max ram to execute, it will return itself as an Invoker.
     *
     * @param ram The amount of RAM required for the function.
     * @return The selected InvokerInterface for function execution.
     * @throws NoPolicyManagerRegistered If there is no PolicyManager registered.
     * @throws NoInvokerAvailable       If there is no available Invoker to execute the function.
     * @throws RemoteException          If an RMI-related exception occurs.
     */
	@Override
	public InvokerInterface selectInvoker(long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException {
		InvokerInterface invoker;

		if (policyManager == null) throw new NoPolicyManagerRegistered("There isn't a policy manager registered.");

		try {
			invoker = policyManager.getInvoker(invokers, ram);
		} catch (NoInvokerAvailable e) {
			if (this.getMaxRam() < ram) throw new NoInvokerAvailable("");
			return this;
		}

		if (invoker.getAvailableRam() - ram >= 0)
			return invoker;

		if (this.getMaxRam() >= ram)
			return this;

		return (invoker);
	}

	/**
     * Sets the PolicyManager for the CompositeInvoker, creating a copy to ensure independence.
     *
     * @param policyManager The PolicyManager to be set.
     * @throws RemoteException If an RMI-related exception occurs.
     */
	@Override
	public void	setPolicyManager(PolicyManager policyManager) throws RemoteException {
		if (policyManager != null)
			this.policyManager = policyManager.copy();
	}

	/**
     * Prepares the Policy Manager of the Composite to a group invocation.
     *
     * @param size The size of the group invocation.
     * @param ram  The amount of RAM for each invocation.
     * @throws RemoteException     If an RMI-related exception occurs.
     * @throws NoInvokerAvailable If there is no available Invoker to invoke.
     */
	@Override
	public void setDistributionPolicyManager(int size, long ram) throws RemoteException, NoInvokerAvailable {
		policyManager.prepareDistribution(invokers, size, ram);
	}

	/**
     * Adds a child Invoker to the list of registered Invokers.
     *
     * @param invoker The child Invoker to be registered.
     * @throws OperationNotValid If the Invoker to register is null or is already in the list.
     * @throws RemoteException   If an RMI-related exception occurs.
     */
	@Override
	public void registerInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException {
		if (invoker == null) throw new OperationNotValid("Invoker to register cannot be null.");
		if (invokers.contains(invoker)) throw new OperationNotValid("Invoker is already registered.");
		invokers.add(invoker);
		invoker.setPolicyManager(policyManager);
	}

	/**
     * Removes a child Invoker from the list of registered Invokers.
     *
     * @param invoker The child Invoker to be removed.
     * @throws OperationNotValid If the Invoker to delete is null or is not in the list.
     * @throws RemoteException   If an RMI-related exception occurs.
     */
	@Override
	public void deleteInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException {
		if (invoker == null) throw new OperationNotValid("Invoker to delete cannot be null.");
		if (!invokers.contains(invoker)) throw new OperationNotValid("Invoker is not registered.");
		invokers.remove(invoker);
	}

}
