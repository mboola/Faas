package core.application;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import java.util.logging.Logger;

import core.exceptions.*;
import core.invoker.InvokerInterface;
import policymanager.PolicyManager;

public class Controller {

	private List<InvokerInterface>	invokers;

	/**
	 * A map containing Invokable functions associated with an unique ID.
	 * @see {@link Invokable} class.
	 */
	private Map<String, Invokable<Object, Object>> invokables;

	private PolicyManager policyManager;

	/**
	 * The instance used to limit the instantiation of Controller class,
	 * following the Singleton design pattern.
	 */
	private static Controller uniqueInstance = null;

	/**
	 * Instantiates a single instance of the Controller class using the Singleton design pattern.
	 * If the instance does not exist, a new Controller instance is created; otherwise, the existing instance is returned.
	 *
	 * @return The unique instance of the Controller class.
	 */
	public static Controller instantiate() {
		if (uniqueInstance == null)
			uniqueInstance = new Controller();
		return (uniqueInstance);
	}

	/**
	 * Constructs a new instance of Controller class, initializing its internal data structures.
	 */
	private Controller() {
		invokers = new LinkedList<InvokerInterface>();
		invokables = new HashMap<String, Invokable<Object, Object>>();
	}


	/**
	 * Sets the PolicyManager for the Controller, affecting the invocation policy for registered actions.
	 * The provided PolicyManager must not be null; otherwise, an OperationNotValid exception is thrown.
	 * If the PolicyManager is successfully set, it is applied to each registered invoker associated with the Controller.
	 *
	 * @param policyManager The Policy Manager to be set for the Controller and invokers.
	 * @throws OperationNotValid If the provided PolicyManager is null.
	 *
	 * @see PolicyManager
	 */
	public void	setPolicyManager(PolicyManager policyManager) throws OperationNotValid {
		if (policyManager == null) throw new OperationNotValid("Policy Manager cannot be null");
		Logger logger = Logger.getLogger(Controller.class.getName());
		this.policyManager = policyManager;
		for (InvokerInterface invoker : invokers) {
			try {
				invoker.setPolicyManager(policyManager);
			} catch (RemoteException e) {
				logger.warning("RemoteException: PolicyManager couldn't be set to " + invoker.toString());
			}
		}
	}

	/**
	 * Adds an invoker to the Controllers list of registered invokers.
	 * 
	 * @param invoker The invoker to be registered.
	 * @throws OperationNotValid If the invoker received as a parameter is null or is already inside the list.
	 */
	public void registerInvoker(InvokerInterface invoker) throws OperationNotValid {
		if (invoker == null) throw new OperationNotValid("Invoker cannot be null.");
		if (invokers.contains(invoker)) throw new OperationNotValid("Invoker is already registered.");
		Logger logger = Logger.getLogger(Controller.class.getName());
		invokers.add(invoker);
		try {
			invoker.setPolicyManager(policyManager);
		} catch (RemoteException e) {
			logger.warning("RemoteException: PolicyManager couldn't be set to " + invoker.toString());
		}
	}

	/**
	 * Removes an invoker from the list of registered invokers.
	 * 
	 * @param invoker The InvokerInterface to be removed.
	 * @throws OperationNotValid If the invoker passed as a parameter is null or is not inside the list.
	 */
	public void deleteInvoker(InvokerInterface invoker) throws OperationNotValid {
		if (invoker == null) throw new OperationNotValid("Invoker to delete cannot be null.");
		if (!invokers.contains(invoker)) throw new OperationNotValid("Invoker is not registered.");
		invokers.remove(invoker);
	}

	/**
	 * Retrieves the list of registered invokers within the controller.
	 *
	 * @return The list of registered invokers.
	 */
	public List<InvokerInterface> getRegisteredInvokers() {
		return (invokers);
	}
	
	/**
	 * Checks whether an action with the specified unique identifier (ID) is already registered in the Controller.
	 *
	 * @param id The unique identifier to check for registration.
	 * @return {@code true} if an action with the specified ID is already registered, {@code false} otherwise.
	 */
	private boolean	isAlreadyRegistered(String id) {
		if (invokables.isEmpty()) return (false);
		return (invokables.get(id) != null);
	}

	/**
	 * Registers an action with the Controller, associating it with a unique identifier (ID),
	 * a function (invokable), and the amount of RAM required for the invocation.
	 *
	 * @param <T>       The type of the input parameter for the invokable function.
	 * @param <R>       The type of the result returned by the invokable function.
	 * @param id        The unique identifier for the registered action.
	 * @param invokable The function to be registered as an invokable action.
	 * @param ram       The amount of RAM required for the invocation of the action.
	 * @throws OperationNotValid If the provided invokable is null, the ID is null, or if the action is already registered.
	 *
	 * @see Invokable
	 * @see Function
	 */
	@SuppressWarnings("unchecked")
	public <T, R> void registerAction(String id, Function<T, R> invokable, long ram) throws OperationNotValid {
		if (invokable == null) throw new OperationNotValid("Action registered cannot be null.");
		if (id == null)	throw new OperationNotValid("ID cannot be null.");
		if (isAlreadyRegistered(id)) throw new OperationNotValid("Action already registered.");
		invokables.put(id, (Invokable<Object, Object>) new Invokable<T, R>(invokable, ram));
	}

	/**
	 * Deletes a registered action from the Controller based on its unique identifier (ID).
	 *
	 * @param id The unique identifier of the action to be deleted.
	 * @throws OperationNotValid If the provided ID is null or if no action is registered with the specified ID.
	 */
	public void deleteAction(String id) throws OperationNotValid {
		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		if (!isAlreadyRegistered(id)) throw new OperationNotValid("There are no actions with the id" + id);
		invokables.remove(id);
	}

	/**
	 * Retrieves the invokable action associated with the specified unique identifier (ID).
	 * Returns the invokable action as an Object. <p> **Used for testing reasons**.
	 *
	 * @param id The unique identifier of the action to be retrieved.
	 * @return The invokable action associated with the specified ID.
	 * @throws OperationNotValid If the provided ID is null or if no action is registered with the specified ID.
	 */
	public Object getAction(String id) throws OperationNotValid {
		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		if (!isAlreadyRegistered(id)) throw new OperationNotValid("There are no actions with the id" + id);
		return invokables.get(id).retrieveInvokable();
	}

	/**
	 * Retrieves and returns the Invokable associated with the specified unique identifier (ID).
	 * The type parameters T and R indicate the input and result types of the retrieved Invokable.
	 *
	 * @param <T> The type of the input parameter for the retrieved Invokable.
	 * @param <R> The type of the result returned by the retrieved Invokable.
	 * @param id The unique identifier of the action whose Invokable is to be retrieved.
	 * @return The Invokable associated with the specified ID.
	 * @throws OperationNotValid If the provided ID is null.
	 * @throws NoActionRegistered If the map of actions is empty or if no action is registered with the specified ID.
	 *
	 * @see Invokable
	 */
	@SuppressWarnings("unchecked")
	public <T, R> Invokable<T,R> retrieveInvokable(String id) throws OperationNotValid, NoActionRegistered {
		if (id == null) throw new OperationNotValid("Id cannot be null.");
		if (invokables.isEmpty()) throw new NoActionRegistered("Map of actions is empty.");
		if (!invokables.containsKey(id)) throw new NoActionRegistered("There is no action registered with that id.");
		return ((Invokable<T, R>)invokables.get(id));
	}

	/**
	 * Selects and returns an InvokerInterface based on the specified RAM requirement using the registered PolicyManager.
	 * The selected Invoker is determined by the PolicyManager's strategy.
	 *
	 * @param ram The amount of RAM required for the invocation.
	 * @return The selected InvokerInterface based on the PolicyManager's strategy.
	 * @throws NoPolicyManagerRegistered If there is no PolicyManager registered with the Controller.
	 * @throws RemoteException If a remote communication-related exception occurs.
	 * @throws NoInvokerAvailable If no suitable Invoker is available based on the PolicyManager's strategy.
	 *
	 * @see PolicyManager
	 * @see InvokerInterface
	 */
	private InvokerInterface selectInvoker(long ram) throws NoPolicyManagerRegistered, RemoteException, NoInvokerAvailable {
		if (policyManager == null) throw new NoPolicyManagerRegistered("There isn't a policy manager registered.");
		return (policyManager.getInvoker(invokers, ram));
	}

	/**
	 * Synchronously invokes the specified Invokable action using the selected Invoker, passing the provided arguments.
	 *
	 * @param <T>       The type of the input parameter for the Invokable.
	 * @param <R>       The type of the result returned by the Invokable.
	 * @param invokable The Invokable action to be invoked.
	 * @param id        The unique identifier associated with the action.
	 * @param args      The input arguments for the Invokable action.
	 * @return The result of invoking the specified Invokable action.
	 * @throws Exception If an exception occurs during the invocation process.
	 */
	private <T, R> R getResult(Invokable<T, R> invokable, String id, T args) throws Exception {
		InvokerInterface invoker = selectInvoker(invokable.getRam());
		return (invoker.invoke(invokable, args, id));
	}

	/**
	 * Asynchronously invokes the specified Invokable action using the selected Invoker, passing the provided arguments.
	 *
	 * @param <T>       The type of the input parameter for the Invokable.
	 * @param <R>       The type of the result returned by the Invokable.
	 * @param invokable The Invokable action to be asynchronously invoked.
	 * @param id        The unique identifier associated with the action.
	 * @param args      The input arguments for the Invokable action.
	 * @return A Future representing the asynchronous result of invoking the specified Invokable action.
	 * @throws Exception If an exception occurs during the asynchronous invocation process.
	 */
	private <T, R> Future<R> getResult_async(Invokable<T, R> invokable, String id, T args) throws Exception {
		InvokerInterface invoker = selectInvoker(invokable.getRam());
		return (invoker.invokeAsync(invokable, args, id));
	}

	/**
	 * Prepares the policy manager and invokes synchronously an Invokable.
	 *
	 * @param <T>  The type of the input parameter for the action.
	 * @param <R>  The type of the result returned by the action.
	 * @param id   The unique identifier associated with the action.
	 * @param args The input arguments for the action.
	 * @return The result of invoking the action.
	 * @throws Exception If an exception occurs during the invocation process.
	 */
	public <T, R> R invoke(String id, T args) throws Exception {
		Invokable<T, R> invokable = retrieveInvokable(id);
		long ram = invokable.getRam();
		policyManager.prepareDistribution(invokers, 1, ram);
		return (getResult(invokable, id, args));
	}
	
	/**
	* Synchronously invokes the action with the specified unique identifier (ID) using the Controller in a group invocation,
	* passing a list of input arguments.
	*
	* @param <T>  The type of the input parameter for the action.
	* @param <R>  The type of the result returned by the action.
	* @param id   The unique identifier associated with the action.
	* @param args The list of input arguments for the group invocation.
	* @return A list of results corresponding to the invocation of the action for each input element.
	* @throws OperationNotValid If the provided list of input arguments is null or empty.
	* @throws Exception         If an exception occurs during the invocation process.
	*/
	public <T, R> List<R> invoke(String id, List<T> args) throws Exception {
		if (args == null) throw new OperationNotValid("List of input in a group invocation cannot be null.");
		if (args.isEmpty()) throw new OperationNotValid("List of input in a group invocation cannot be empty.");

		Invokable<T, R> invokable = retrieveInvokable(id);
		long ram = invokable.getRam();
		policyManager.prepareDistribution(invokers, args.size(), ram);

		List<R> result = new LinkedList<R>();
		for (T element : args)
			result.add(getResult(invokable, id, element));
		return (result);
	}

	/**
	 * Prepares the policy manager and invokes asynchronously an Invokable.
	 *
	 * @param <T>  The type of the input parameter for the action.
	 * @param <R>  The type of the result returned by the action.
	 * @param id   The unique identifier associated with the action.
	 * @param args The input arguments for the action.
	 * @return The result of invoking the action.
	 * @throws Exception If an exception occurs during the invocation process.
	 */
	public <T, R> Future<R> invoke_async(String id, T args) throws Exception {
		Invokable<T, R> invokable = retrieveInvokable(id);
		long ram = invokable.getRam();
		policyManager.prepareDistribution(invokers, 1, ram);
		return (getResult_async(invokable, id, args));
	}

	/**
	 * Asynchronously invokes the action with the specified unique identifier (ID) using the Controller in a group invocation,
	 * passing a list of input arguments.
	 *
	 * @param <T>  The type of the input parameter for the action.
	 * @param <R>  The type of the result returned by the action.
	 * @param id   The unique identifier associated with the action.
	 * @param args The list of input arguments for the group invocation.
	 * @return A list of Future objects representing the asynchronous results of invoking the action for each input element.
	 * @throws OperationNotValid If the provided list of input arguments is null or empty.
	 * @throws Exception         If an exception occurs during the asynchronous invocation process.
	 */
	public <T, R> List<Future<R>> invoke_async(String id, List<T> args) throws Exception {
		if (args == null) throw new OperationNotValid("List of input in a group invocation cannot be null.");
		if (args.isEmpty()) throw new OperationNotValid("List of input in a group invocation cannot be empty.");

		Invokable<T, R> invokable = retrieveInvokable(id);
		long ram = invokable.getRam();
		policyManager.prepareDistribution(invokers, args.size(), ram);

		List<Future<R>> result = new LinkedList<Future<R>>();
		for (T element : args)
			result.add(getResult_async(invokable, id, element));
		return (result);
	}

	/**
	 * Lists the available actions along with their allocated RAM.
	*/
	@SuppressWarnings("rawtypes")
	public void listActions()  {
		if (invokables.isEmpty()) {
			System.out.println("No actions found:");
			return ;
		}
		System.out.println("List of Actions and their RAM value:");
		for (Map.Entry<String, Invokable<Object, Object>> entry : invokables.entrySet()) {
			String key = entry.getKey();
			Invokable invokable = entry.getValue();
			long ram = invokable.getRam();
			System.out.println("Action with ID: " + key + " | Allocated RAM: " + ram + " megabytes");
		}
	}

	/**
	 * Shuts down all registered invokers.
	 * 
	 * @throws RemoteException If a remote communication error occurs while shutting down an invoker.
	 */
	public void shutdownAllInvokers() throws RemoteException 
	{
		Logger logger = Logger.getLogger(Controller.class.getName());
		for (InvokerInterface invoker : invokers) {
			try {
				invoker.shutdownInvoker();
			} catch (RemoteException e) {
				logger.warning("RemoteException: PolicyManager couldn't be set to " + invoker.toString());
			}
		}
	}

}
