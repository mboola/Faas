package core.invoker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.Future;

import core.application.Invokable;
import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.exceptions.OperationNotValid;
import policymanager.PolicyManager;

/**
 * The interface defining the contract for an Invoker in a distributed computing environment.
 * Invokers are responsible for executing functions and managing resources.
 *
 * @see Remote
 */
public interface InvokerInterface extends Remote {

	/**
	 * Gets the unique identifier of the Invoker.
	 *
	 * @return The unique identifier of the Invoker.
	 * @throws RemoteException If a remote communication-related exception occurs.
	 */
	public String getId() throws RemoteException;
	
	/**
	 * Retrieves the amount of RAM currently in use by the Invoker.
	 *
	 * @return The amount of RAM currently in use.
	 * @throws RemoteException If a remote communication-related exception occurs.
	 */
	public long	getUsedRam() throws RemoteException;

	/**
	 * Retrieves the available RAM that can be used by the Invoker.
	 *
	 * @return The available RAM for use.
	 * @throws RemoteException If a remote communication-related exception occurs.
	 */
	public long	getAvailableRam() throws RemoteException;
	
	/**
	 * Reserves a specified amount of RAM in MegaBytes for use by the Invoker.
	 *
	 * @param ram The amount of RAM to reserve in MegaBytes.
	 * @throws RemoteException If a remote communication-related exception occurs.
	 */
	public void reserveRam(long ram) throws RemoteException;

	/**
	 * Retrieves the maximum amount of RAM that can be used by the Invoker.
	 *
	 * @return The maximum RAM that can be used by this Invoker.
	 * @throws RemoteException If a remote communication-related exception occurs.
	 */
	public long	getMaxRam() throws RemoteException;

	/**
	 * Selects an Invoker that has enough RAM to execute an Invokable consuming the specified RAM.
	 *
	 * @param ram The amount of RAM in MegaBytes that the Invokable will consume.
	 * @return The Invoker that will execute the function.
	 * @throws NoPolicyManagerRegistered If there is no PolicyManager registered with the Invoker.
	 * @throws NoInvokerAvailable        If there is no Invoker with enough max RAM or a remote communication-related exception occurs.
	 * @throws RemoteException           If a remote communication-related exception occurs.
	 */
	public InvokerInterface selectInvoker(long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException;

	/**
	 * Executes a synchronous function passed by reference, applying observers and decorators.
	 *
	 * @param <T>       The type of the input parameter for the Invokable.
	 * @param <R>       The type of the result returned by the Invokable.
	 * @param invokable The function to be executed and the RAM it consumes.
	 * @param args      The arguments needed by the function, of type T.
	 * @param id        The identifier of the function, needed by observers and decorators to store data correctly.
	 * @return The result of the function invoked, of type R.
	 * @throws Exception If an exception occurs during the invocation process.
	 */
	public <T, R> R invoke(Invokable<T, R> invokable, T args, String id) throws Exception;

	/**
	 * Executes an asynchronous function passed by reference.
	 * If there is no space in the pool of threads, it waits and then gets invoked.
	 *
	 * @param <T>       The type of the input parameter for the Invokable.
	 * @param <R>       The type of the result returned by the Invokable.
	 * @param invokable The function to be executed asynchronously and the RAM it consumes.
	 * @param args      The arguments needed by the function, of type T.
	 * @param id        The identifier of the function, needed by observers and decorators to store data correctly.
	 * @return A Future representing the asynchronous result of invoking the function.
	 * @throws Exception If an exception occurs during the asynchronous invocation process.
	 */
	public <T, R> Future<R> invokeAsync(Invokable<T, R> invokable, T args, String id) throws Exception;

	/**
	 * Shuts down the executor of the Invoker. Must be called when the application finishes.
	 *
	 * @throws RemoteException If a remote communication-related exception occurs.
	 */
	public void shutdownInvoker() throws RemoteException;

	/**
	 * Sets the PolicyManager for the Invoker.
	 *
	 * @param policyManager The PolicyManager to be set.
	 * @throws RemoteException If a remote communication-related exception occurs.
	 */
	public void	setPolicyManager(PolicyManager policyManager) throws RemoteException;

	/**
	 * If needed prepares the PolicyManager for a group invocation.
	 *
	 * @param size The size parameter for the Distribution PolicyManager.
	 * @param ram  The RAM parameter for the Distribution PolicyManager.
	 * @throws NoInvokerAvailable If there is no Invoker available based on the Distribution PolicyManager's strategy.
	 * @throws RemoteException    If a remote communication-related exception occurs.
	 */
	public void setDistributionPolicyManager(int size, long ram) throws NoInvokerAvailable, RemoteException;
	
	/**
	 * Registers an Invoker to a list in case this is a CompositeInvoker.
	 *
	 * @param invoker The Invoker to be registered.
	 * @throws OperationNotValid If the provided Invoker is null or if the Invoker is already registered.
	 * @throws RemoteException   If a remote communication-related exception occurs.
	 */
	public void registerInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException;

	/**
	 * Deletes an Invoker from a list in case this is a CompositeInvoker.
	 *
	 * @param invoker The Invoker to be registered.
	 * @throws OperationNotValid If the provided Invoker is null or if the Invoker is already registered.
	 * @throws RemoteException   If a remote communication-related exception occurs.
	 */
	public void deleteInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException;
}
