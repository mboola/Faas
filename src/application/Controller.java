package application;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import dynamic_proxy.DynamicProxy;
import faas_exceptions.NoActionRegistered;
import faas_exceptions.NoPolicyManagerRegistered;
import faas_exceptions.OperationNotValid;
import invoker.InvokerInterface;
import policy_manager.PolicyManager;

public class Controller {

	/**
	 * List of invokers used for executing functions. This will be implementations of the interface InvokerInterface and can be either simple or composite.
	 *  The implementations of InvokerInterface can be:
	 * 	An Invoker that executes functions.
	 *	An Invoker that executes functions and has invokers inside of him that executes functions.
	 *	A ServerInvoker that executes functions remotly.
	 */
	private List<InvokerInterface>	invokers;

	/**
	 * A map containing Invokable functions associated with an unique identifiers in the Controller.
	 * An invokable function encapsulates a predefined functionality and is represented by an instance of the {@link Invokable} class.
	 */
	private Map<String, Invokable>	invokables;

	/**
	 * Determines what policy will be used to select an invoker.
	 */
	private PolicyManager	policyManager;

	/**
	 * The instance used to limit the instantiation of Controller class, following the Singleton design pattern.
	 */
	private static Controller	uniqueInstance = null;

	/**
	 * Checks if the Controller is instanciated, creates one if it isn't.
	 * 
	 * @return The Singleton instance of Controller. 
	 */
	public static Controller instantiate() {
		if (uniqueInstance == null)
			uniqueInstance = new Controller();
		return (uniqueInstance);
	}

	/**
	 * Constructs a new instance of Controller and instantiates all the structs it uses.
	 */
	private Controller() {
		invokers = new LinkedList<InvokerInterface>();
		invokables = new HashMap<String, Invokable>();
	}

	/**
	 * Sets the Policy Manager for the Controller and propagates it to registered invokers.
	 * 
	 * @param policyManager The Policy Manager to be set for the Controller and invokers.
	 * @throws OperationNotValid If the Policy Manager is null.
	 */
	public void	setPolicyManager(PolicyManager policyManager) throws Exception
	{
		if (policyManager == null) throw new OperationNotValid("Policy Manager cannot be null");
		this.policyManager = policyManager;
		for (InvokerInterface invoker : invokers)
			invoker.setPolicyManager(policyManager);
	}

	/**
	 * Adds an invoker to the Controllers list of registered invokers.
	 * 
	 * @param invoker The invoker to be registered.
	 * @throws OperationNotValid If the invoker received as a parameter is null or is already inside the list.
	 * @throws NoPolicyManagerRegistered If no policy manager is registered with the controller.
	 */
	public void registerInvoker(InvokerInterface invoker) throws Exception
	{
		if (invoker == null) throw new OperationNotValid("Invoker cannot be null.");
		if (invokers.contains(invoker)) throw new OperationNotValid("Invoker is already registered.");
		invokers.add(invoker);
		invoker.setPolicyManager(policyManager);
	}

	/**
	 * Removes an invoker from the list of registered invokers.
	 * 
	 * @param invoker The InvokerInterface to be removed.
	 * @throws OperationNotValid If the invoker passed as a parameter is null or is not inside the list.
	 */
	public void deleteInvoker(InvokerInterface invoker) throws OperationNotValid
	{
		if (invoker == null) throw new OperationNotValid("Invoker to delete cannot be null.");
		if (!invokers.contains(invoker)) throw new OperationNotValid("Invoker is not registered.");
		invokers.remove(invoker);
	}

	/**
	 * Retrieves the list of registered invokers within the controller.
	 *
	 * @return The list of registered invokers.
	 */
	public List<InvokerInterface> getRegisteredInvokers()
	{
		return (invokers);
	}
	
	/**
	 * Checks if the id of an action that we are trying to register is already registered.
	 * 
	 * @param id The id of the action we are trying to register.
	 * @return False if the map is empty or the id doesn't exists in it. True if it does.
	 */
	private boolean	isAlreadyRegistered(String id)
	{
		if (invokables.isEmpty()) return (false);
		return (invokables.get(id) != null);
	}

	/** //TODO: Redo this one
	 * Tries to register an action.
	 * 
	 * <p>The action to register can be:</p>
	 * <ul>
	 * 	<li>A Function<T, R>.</li>
	 * 	<li>An implementation of the interface Action<T, R>.</li>
	 * 	<li>A class with methods that will be used in reflection. TODO: change this case explanation</li>
	 * </ul>
	 * @param id The id of the object to be registered.
	 * @param invokable The object that can be invoked.
	 * @param ram The ram in MegaBytes this invokation will consume.
	 * @throws OperationNotValid The exception can be caused because:
	 * <ul>
	 * 	<li>The object we are tring to register is null.</li>
	 * 	<li>The id we are tring to register is null.</li>
	 * 	<li>The id we are tring to register already exists.</li>
	 * </ul>
	 */
	public void registerAction(String id, Object invokable, long ram) throws OperationNotValid
	{
		if (invokable == null) throw new OperationNotValid("Action registered cannot be null.");
		if (id == null)	throw new OperationNotValid("ID cannot be null.");
		if (isAlreadyRegistered(id)) throw new OperationNotValid("Action already registered.");
		invokables.put(id, new Invokable(id, invokable, ram));
	}

	/**
	 * Tries to delete an action.
	 * 
	 * @param id The id of the object to be deleted.
	 * @throws OperationNotValid The exception can be caused because:
	 * <ul>
	 * 	<li>The id of the object we are tring to delete is null.</li>
	 * 	<li>The id of the object we are tring to delete doesn't exists in the map.</li>
	 * </ul>
	 */
	public void deleteAction(String id) throws OperationNotValid
	{
		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		if (!isAlreadyRegistered(id)) throw new OperationNotValid("There are no actions with the id" + id);
		invokables.remove(id);
	}

	/**
	 * Gets the action stored with the ID passed as a parameter.
	 * 
	 * @param id ID of the action to get.
	 * @return The action or null if none was found.
	 */
	public Object getAction(String id)
	{
		if (id == null) return (null);
		return (invokables.get(id).getInvokable());
	}

	/**
	 * Method used by actions to get the invokable.
	 * 
	 * @param id Identifier of the action.
	 * @return The invokable or null if none was found.
	 * @throws NoActionRegistered //TODO this
	 */
	private Invokable getInvokable(String id) throws OperationNotValid, NoActionRegistered
	{
		if (id == null) throw new OperationNotValid("Id cannot be null.");
		if (invokables.isEmpty()) throw new NoActionRegistered("Map of actions is empty.");
		if (!invokables.containsKey(id)) throw new NoActionRegistered("There is no action registered with that id.");
		return (invokables.get(id));
	}

	//TODO: Generic throws is ugly maybe fix later? Applies for most below.

	/**
	 * Method used to select a invoker to execute a function based on the ram it consumes and the policy we have assigned.
	 * @param ram
	 * @return
	 * @throws Exception The exception can be caused because:
	 * <ul>
	 * 	<li>NoPolicyManagerRegistered: There is no policyManager registered.</li>
	 * 	<li>NoInvokerAvailable: There is no invoker with enough max ram to execute the invokable.</li>
	 *  <li>Exeption: something goes wrong with RMI.</li>
	 * </ul>
	 */
	private InvokerInterface selectInvoker(long ram) throws Exception
	{
		if (policyManager == null) throw new NoPolicyManagerRegistered("There isn't a policy manager registered.");
		return (policyManager.getInvoker(invokers, ram));
	}

	/**
	 * Method used by invokations to get an invoker to execute code and then execute it.
	 * 
	 * @param <T> Datatype of the parameters of the function to be invoked.
	 * @param <R> Datatype of the return of the function to be invoked.
	 * @param invokable Function to be invoked.
	 * @param id Identifier of the invokable to be invoked.
	 * @param args Parameters of the invokable.
	 * @return Result of the invokation of the invokable.
	 * @throws Exception The exception can be caused because:
	 * <ul>
	 * 	<li>There is no invoker with enough max ram to execute the invokable.</li>
	 * 	<li>Something goes wrong when executing the invokable.</li>
	 * </ul>
	 */
	private <T, R> R getResult(Invokable invokable, String id, T args) throws Exception
	{
		InvokerInterface	invoker;

		invoker = selectInvoker(invokable.getRam());
		return (invoker.invoke(invokable, args, id));
	}

	//TODO: javadoc this
	private <T, R> Future<R> getResult_async(Invokable invokable, String id, T args) throws Exception
	{
		InvokerInterface	invoker;

		invoker = selectInvoker(invokable.getRam());
		return (invoker.invokeAsync(invokable, args, id));
	}

	/**
	 * Searches an action with the ID passed as a parameter and invokes it with args as a parameters.
	 * 
	 * @param <T> Datatype of the parameters of the function to be invoked.
	 * @param <R> Datatype of the return of the function to be invoked.
	 * @param id Identifier of the action to be invoked.
	 * @param args Parameters of the action.
	 * @return Result of the invokation of the action.
	 * @throws Exception <p>The exception can be caused because:</p>
	 * <ul>
	 *  <li>The id passed as a parameter is null.</li>
	 *  <li>There is no action found with the id passed as a parameter.</li>
	 * 	<li>There is no invoker with enough max ram to run execute the action.</li>
	 * 	<li>Something goes wrong when executing the action.</li>
	 * </ul>
	 */
	public <T, R> R invoke(String id, T args) throws Exception
	{
		return (getResult(getInvokable(id), id, args));
	}

	/**
	 * Searches an action with the ID passed as a parameter and invokes it for each argument of list args as a parameters.
	 * 
	 * @param <T> Datatype of the parameters of the function to be invoked.
	 * @param <R> Datatype of the return of the function to be invoked.
	 * @param id Identifier of the action to be invoked.
	 * @param args List of parameters of the action.
	 * @return List of results of the invokation of the action.
	 * @throws Exception The exception can be caused because:
	 * <ul>
	 *  <li>The id passed as a parameter is null.</li>
	 *  <li>There is no action found with the id passed as a parameter.</li>
	 * 	<li>There is no invoker with enough max ram to run execute the invokable.</li>
	 * 	<li>Something goes wrong when executing the action.</li>
	 * </ul>
	 */
	public <T, R> List<R> invoke(String id, List<T> args) throws Exception
	{
		Invokable	invokable;
		List<R> 	result;

		invokable = getInvokable(id);
		result = new LinkedList<R>();
		for (T element : args)
			result.add(getResult(invokable, id, element));
		return (result);
	}

	/**
	 * Searches an action with the ID passed as a parameter and invokes it for each argument of list args as a parameters using asynchronous invokations.
	 * 
	 * @param <T> Datatype of the parameters of the function to be invoked.
	 * @param <R> Datatype of the return of the function to be invoked.
	 * @param id Identifier of the action to be invoked.
	 * @param args Parameters of the action.
	 * @return Future result of the invokation of the action.
	 * @throws Exception The exception can be caused because:
	 * <ul>
	 *  <li>The id passed as a parameter is null.</li>
	 *  <li>There is no action found with the id passed as a parameter.</li>
	 * 	<li>There is no invoker with enough max ram to run execute the action.</li>
	 * 	<li>Something goes wrong when executing the action.</li>
	 * </ul>
	 */
	public <T, R> Future<R> invoke_async(String id, T args) throws Exception
	{
		return (getResult_async(getInvokable(id), id, args));
	}

	/**
	 * Invokes an action with the specified ID asynchronously, providing a list of arguments for each invocation.
	 *
	 * @param <T>  Datatype of the parameters of the function to be invoked.
	 * @param <R>  Datatype of the return of the function to be invoked.
	 * @param id   Identifier of the action to be invoked.
	 * @param args List of parameters for each asynchronous invocation.
	 * @return List of Future results of the invocations of the action.
	 * @throws Exception The exception can be caused because:
	 * <ul>
	 *   <li>The id passed as a parameter is null.</li>
	 *   <li>There is no action found with the id passed as a parameter.</li>
	 *   <li>There is no invoker with enough max ram to run execute the action.</li>
	 *   <li>Something goes wrong when executing one or more actions.</li>
	 * </ul>
	 * //TODO: Specify more details about the potential exceptions.
	 */
	public <T, R> List<Future<R>> invoke_async(String id, List<T> args) throws Exception
	{
		Invokable		invokable;
		List<Future<R>>	result;

		invokable = getInvokable(id);
		result = new LinkedList<Future<R>>();
		for (T element : args)
			result.add(getResult_async(invokable, id, element));
		return (result);
	}

	/**
	 * Retrieves a proxy object for an action with the specified ID.
	 *
	 * @param id Identifier of the action.
	 * @return Proxy object for the action.
	 * @throws Exception The exception can be caused because:
	 * <ul>
	 *   <li>The id passed as a parameter is null.</li>
	 *   <li>There is no action found with the id passed as a parameter.</li>
	 * </ul>
	 * //TODO: Specify more details about the potential exceptions.
	 */
	public Object getActionProxy(String id) throws Exception
	{
		return (DynamicProxy.instantiate(getInvokable(id).getInvokable()));
	}

	/**
 	 * Lists the available actions along with their allocated RAM.
 	 */
	public void listActions() 
	{
		if (invokables.isEmpty()) {
			System.out.println("No actions found:");
			return;
		}
		System.out.println("List of Actions and their RAM value:");
		for (Map.Entry<String, Invokable> entry : invokables.entrySet()) {
			String key = entry.getKey();
			Invokable invokable = entry.getValue();
			long ram = invokable.getRam();
			System.out.println("Action with ID: " + key + " | Allocated RAM: " + ram + " bytes"); //TODO: is it in bytes?
		}
	}

	/**
 	 * Lists the available invokers and their available RAM.
	 *
	 * @throws RemoteException If a remote communication error occurs while accessing an invoker's RAM. //TODO: is this because of RMI?
 	 */
	public void listInvokersRam() throws RemoteException 
	{
		int i = 1;
		for (InvokerInterface invoker:invokers) {
			System.out.println("Invoker "+i+" ram: "+invoker.getAvailableRam());
			i++;
		}
	}

	/**
	 * Shuts down all registered invokers.
	 * 
	 * @throws RemoteException If a remote communication error occurs while shutting down an invoker.
	 */
	public void shutdownAllInvokers() throws RemoteException 
	{
		for (InvokerInterface invoker:invokers) {
			invoker.shutdownInvoker();
		}
	}

}