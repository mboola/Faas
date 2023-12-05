package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import RMI.InvokerInterface;
import dynamic_proxy.DynamicProxy;
import faas_exceptions.NoActionRegistered;
import faas_exceptions.OperationNotValid;
import policy_manager.PolicyManager;

public class Controller {
	private Map<String, Action>	actions;
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
		actions = new HashMap<String, Action>();
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

	public Object getAction(String id)
	{
		if (id == null) return (null);
		return (invokables.get(id).getInvokable());
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

	//used the register an Action in the controller. Ram must be inputed in MegaBytes
	public void registerAction(String id, Object f, int ram)
	{
		if ( hasMapAction(id) == null)
		{
			actions.put(id, new Action(ram, f, id));
			return ;
		}
		//TODO: throw error. already exists
	}

	public void	listActions()
	{
		//TODO: is this all the info I wanna show?
		if ( actions.isEmpty())
			return ;
		for(String key : actions.keySet())
			System.out.println(key);
	}
	
	public void	addPolicyManager(PolicyManager policyManager)
	{
		this.policyManager = policyManager;
	}

	private InvokerInterface selectInvoker(int ram) throws Exception
	{
		return (policyManager.getInvoker(invokers, ram));
	}

	private <T, R> R getResult(Action action, T args, String id) throws Exception
	{
		InvokerInterface	invoker;

		invoker = selectInvoker(action.getRam());
		return (invoker.invoke(action, args, id));
	}

	public <T, R> R invoke(String id, T args) throws Exception
	{
		Action	action;

		action = hasMapAction(id);
		if ( action == null )
			throw new NoActionRegistered("There are no actions with the id" + id);
		return (getResult(action, args, id));
	}

	public <T, R> List<R> invoke(String id, List<T> args) throws Exception
	{
		Action	action;
		List<R> result;

		action = hasMapAction(id);
		if ( action == null )
			throw new NoActionRegistered("There are no actions with the id" + id);
		result = new LinkedList<R>();
		for (T element : args)
			result.add(getResult(action, element, id));
		return (result);
	}

	public <T, R> Future<R> invoke_async(String id, T args) throws Exception
	{
		Action	action;

		action = hasMapAction(id);
		if ( action == null )
			throw new NoActionRegistered("There are no actions with the id" + id);
		return (selectInvoker(action.getRam()).invokeAsync(action, args, id));
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
