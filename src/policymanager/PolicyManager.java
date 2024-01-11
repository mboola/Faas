package policymanager;

import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

/**
 * Interface for defining different policy managers to select invokers.
 */
public interface PolicyManager {

	/**
	 * Retrieves an invoker based on the policy.
	 *
	 * @param invokers The list of invokers.
	 * @param ram      The amount of required RAM for the invocation.
	 * @return The selected invoker.
	 * @throws NoPolicyManagerRegistered If the invoker selected is a composite and his policy manager is not properly registered.
	 * @throws NoInvokerAvailable        If no invoker has enough max ram to execute the function.
	 * @throws RemoteException           If a remote exception occurs.
	 */
	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram)
			throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException;

	/**
	 * Creates a copy of the policy manager.
	 *
	 * @return A new instance of the policy manager.
	 */
	public PolicyManager copy();

	/**
	 * Prepares the distribution of invocations based on the policy.
	 *
	 * @param invokers           The list of invokers.
	 * @param size               The size of the distribution.
	 * @param ram                The amount of required RAM for the invocation.
	 * @param singleInvocation   Indicates whether it is a single or a group invocation.
	 * @throws NoInvokerAvailable If no invoker has enough max ram to execute the function.
	 * @throws RemoteException    If a remote exception occurs.
	 */
	public void	prepareDistribution(List<InvokerInterface> invokers, int size, long ram)
			throws NoInvokerAvailable, RemoteException;
}
