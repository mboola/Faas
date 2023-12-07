package application;
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
	public MetricSet			metrics;

	/**
	 * List of invokables our controller has.	//TODO: extend this explanation
	 */
	private Map<String, Invokable>	invokables;

	/**
	 * List of invokers used for executing functions. This will be implementations of the interface InvokerInterface.
	 * <p>The implementations of InvokerInterface can be:</p>
	 * <ul>
	 * 	<li>An Invoker that executes functions.</li>
	 *	<li>An Invoker that executes functions and has invokers inside of him that executes functions.</li>
	 *	<li>A ServerInvoker that executes functions remotly.</li>
	 * </ul>
	 */
	private List<InvokerInterface>	invokers;

	/**
	 * Policy used to select an invoker from invokers to execute a function.
	 */
	private PolicyManager	policyManager;

	/**
	 * The instance used to limit the instantiation of Controller class.
	 */
	private static Controller	unicInstance = null;

	/**
	 * Checks if the instance is null, and creates one if it is.
	 * 
	 * @return The singleton instance of Controller. 
	 */
	public static Controller instantiate() {
		if (unicInstance == null)
			unicInstance = new Controller();
		return (unicInstance);
	}

	/**
	 * Constructs a new instance of Controller and instantiates all the structs it uses.
	 */
	private Controller() {
		invokers = new LinkedList<InvokerInterface>();
		invokables = new HashMap<String, Invokable>();
		metrics = new MetricSet();
	}

	/**
	 * Adds the implementation of InvokerInterface to the list of registered invokers.
	 * 
	 * @param invoker The InvokerInterface to be registered.
	 * @throws OperationNotValid If the invoker passed as a parameter is null or is already inside the list.
	 */
	public void registerInvoker(InvokerInterface invoker) throws OperationNotValid
	{
		if (invoker == null) throw new OperationNotValid("Invoker to register cannot be null.");
		if (invokers.contains(invoker)) throw new OperationNotValid("Invoker is already registered.");
		invokers.add(invoker);
		invoker.setPolicyManager(policyManager);
	}

	/**
	 * Removes the implementation of InvokerInterface from the list of registered invokers.
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
	 * Getter of the list of type InvokerInterface named invokers.
	 * //TODO: name this differently.
	 */
	public List<InvokerInterface>	getInvokerInterfaces()
	{
		return (invokers);
	}
	
	/**
	 * Checks if the id of the object we are trying to register is already registered.
	 * 
	 * @param id The id of the object we are trying to register.
	 * @return False if the map is empty or the id doesn't exists in it. True if it does.
	 */
	private boolean	isAlreadyRegistered(String id)
	{
		if (invokables.isEmpty())
			return (false);
		return (invokables.get(id) != null);
	}

	/**
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
	 * @throws OperationNotValid <p>The exception can be caused because:</p>
	 * <ul>
	 * 	<li>The object we are tring to register is null.</li>
	 * 	<li>The id we are tring to register is null.</li>
	 * 	<li>The id we are tring to register already exists.</li>
	 * </ul>
	 */
	public void registerAction(String id, Object invokable, long ram) throws OperationNotValid
	{
		if (invokable == null) throw new OperationNotValid("Invokable registered cannot be null.");
		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		if (isAlreadyRegistered(id)) throw new OperationNotValid("Invokable already registered.");
		invokables.put(id, new Invokable(id, invokable, ram));
	}

	/**
	 * Tries to delete an action.
	 * 
	 * @param id The id of the object to be deleted.
	 * @throws OperationNotValid <p>The exception can be caused because:</p>
	 * <ul>
	 * 	<li>The id of the object we are tring to delete is null.</li>
	 * 	<li>The id of the object we are tring to delete doesn't exists in the map.</li>
	 * </ul>
	 */
	public void removeAction(String id) throws OperationNotValid
	{
		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		if (!isAlreadyRegistered(id)) throw new OperationNotValid("There are no actions with the id" + id);
		invokables.remove(id);
	}

	/**
	 * Gets the invokable stored with the id passed as a parameter.
	 * 
	 * @param id Id of the invokable to get.
	 * @return The invokable or null if none was found.
	 */
	public Object getAction(String id)
	{
		if (id == null) return (null);
		return (invokables.get(id).getInvokable());
	}

	//TODO: define javadoc
	public void	setPolicyManager(PolicyManager policyManager) throws OperationNotValid
	{
		if (policyManager == null) throw new OperationNotValid("Policy Manager cannot be null");
		this.policyManager = policyManager;
		for (InvokerInterface invoker : invokers)
			invoker.setPolicyManager(policyManager);
	}

	/**
	 * Method used to select a invoker to execute a function based on the ram it consumes and the policy we have assigned.
	 * @param ram
	 * @return
	 * @throws Exception <p>The exception can be caused because:</p>
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
	 * Method used by invokations to get the invokable.
	 * 
	 * @param id Identifier of the invokable.
	 * @return The invokable or null if none was found.
	 */
	private Invokable getInvokable(String id)
	{
		if ( invokables.isEmpty() )
			return (null);
		return (invokables.get(id));
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
	 * @throws Exception <p>The exception can be caused because:</p>
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

	private <T, R> Future<R> getResult_async(Invokable invokable, String id, T args) throws Exception
	{
		InvokerInterface	invoker;

		invoker = selectInvoker(invokable.getRam());
		return (invoker.invokeAsync(invokable, args, id));
	}

	/**
	 * Searches a invokable with the id passed as a parameter and invokes it with args as a parameters.
	 * 
	 * @param <T> Datatype of the parameters of the function to be invoked.
	 * @param <R> Datatype of the return of the function to be invoked.
	 * @param id Identifier of the invokable to be invoked.
	 * @param args Parameters of the invokable.
	 * @return Result of the invokation of the invokable.
	 * @throws Exception <p>The exception can be caused because:</p>
	 * <ul>
	 *  <li>The id passed as a parameter is null.</li>
	 *  <li>There is no invokable found with the id passed as a parameter.</li>
	 * 	<li>There is no invoker with enough max ram to run execute the invokable.</li>
	 * 	<li>Something goes wrong when executing the invokable.</li>
	 * </ul>
	 */
	public <T, R> R invoke(String id, T args) throws Exception
	{
		Invokable	invokable;

		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		invokable = getInvokable(id);
		if (invokable == null) throw new OperationNotValid("There are no invokables registered with the id" + id);
		return (getResult(invokable, id, args));
	}

	/**
	 * Searches a invokable with the id passed as a parameter and invokes it n times with each arg of list args as a parameters.
	 * 
	 * @param <T> Datatype of the parameters of the function to be invoked.
	 * @param <R> Datatype of the return of the function to be invoked.
	 * @param id Identifier of the invokable to be invoked.
	 * @param args List of parameters of the invokable.
	 * @return List of results of the invokation of the invokable.
	 * @throws Exception <p>The exception can be caused because:</p>
	 * <ul>
	 *  <li>The id passed as a parameter is null.</li>
	 *  <li>There is no invokable found with the id passed as a parameter.</li>
	 * 	<li>There is no invoker with enough max ram to run execute the invokable.</li>
	 * 	<li>Something goes wrong when executing the invokable.</li>
	 * </ul>
	 */
	public <T, R> List<R> invoke(String id, List<T> args) throws Exception
	{
		Invokable	invokable;
		List<R> 	result;

		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		invokable = getInvokable(id);
		if (invokable == null) throw new OperationNotValid("There are no invokables registered with the id" + id);
		result = new LinkedList<R>();
		for (T element : args)
			result.add(getResult(invokable, id, element));
		return (result);
	}

	/**
	 * Searches a invokable with the id passed as a parameter and invokes it n times with each arg of list args as a parameters.
	 * 
	 * @param <T>
	 * @param <R>
	 * @param id
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public <T, R> Future<R> invoke_async(String id, T args) throws Exception
	{
		Invokable	invokable;

		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		invokable = getInvokable(id);
		if (invokable == null) throw new OperationNotValid("There are no invokables registered with the id" + id);
		return (getResult_async(invokable, id, args));
	}

	//TODO: invoke_async list of args
	public <T, R> List<Future<R>> invoke_async(String id, List<T> args) throws Exception
	{
		Invokable		invokable;
		List<Future<R>>	result;

		if (id == null)	throw new OperationNotValid("Id cannot be null.");
		invokable = getInvokable(id);
		if (invokable == null) throw new OperationNotValid("There are no invokables registered with the id" + id);
		result = new LinkedList<Future<R>>();
		for (T element : args)
			result.add(getResult_async(invokable, id, element));
		return (result);
	}

	public Object getActionProxy(String id) throws Exception
	{
		Action action = hasMapAction(id);
		if ( action == null )
			throw new NoActionRegistered("There are no actions with the id" + id);
		return (DynamicProxy.instantiate(action.getFunction()));
	}

	//TODO: javadoc all below this

	/* Used to search if we already have this action in our map */
	public Action hasMapAction(String id)
	{
		if ( actions.isEmpty() )
			return (null);
		return (actions.get(id));
	}

	public void	listActions()
	{
		//TODO: is this all the info I wanna show?
		if ( actions.isEmpty())
			return ;
		for(String key : actions.keySet())
			System.out.println(key);
	}

	public void listInvokersRam() throws Exception
	{
		int i = 1;
		for (InvokerInterface invoker:invokers) {
			System.out.println("Invoker "+i+" ram: "+invoker.getAvailableRam());
			i++;
		}
	}

	public void shutdownAllInvokers() throws Exception
	{
		for (InvokerInterface invoker:invokers) {
			invoker.shutdownInvoker();
		}
	}

}
