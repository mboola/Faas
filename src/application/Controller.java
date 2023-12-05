package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import RMI.InvokerInterface;
import dynamic_proxy.DynamicProxy;
import faas_exceptions.InvokerNotValid;
import faas_exceptions.NoActionRegistered;
import policy_manager.PolicyManager;

public class Controller {
	private Map<String, Action>	actions;
	public MetricSet			metrics;

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
		actions = new HashMap<String, Action>();
		metrics = new MetricSet();
	}

	/**
	 * Adds the implementation of InvokerInterface to the list of registered invokers.
	 * 
	 * @param invoker The InvokerInterface to be registered.
	 * @throws InvokerNotValid if the invoker passed as a parameter is null or is already inside the list.
	 */
	public void registerInvoker(InvokerInterface invoker) throws InvokerNotValid
	{
		if (invoker == null) throw new InvokerNotValid("Invoker to register cannot be null.");
		if (invokers.contains(invoker)) throw new InvokerNotValid("Invoker is already registered.");
		invokers.add(invoker);
	}

	/**
	 * Removes the implementation of InvokerInterface from the list of registered invokers.
	 * 
	 * @param invoker The InvokerInterface to be removed.
	 * @throws InvokerNotValid if the invoker passed as a parameter is null or is not inside the list.
	 */
	public void deleteInvoker(InvokerInterface invoker) throws InvokerNotValid
	{
		if (invoker == null) throw new InvokerNotValid("Invoker to delete cannot be null.");
		if (!invokers.contains(invoker)) throw new InvokerNotValid("Invoker is not registered.");
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

	public void removeAction(String id) throws Exception
	{
		if ( hasMapAction(id) == null )
			throw new NoActionRegistered("There are no actions with the id" + id);
		else
			actions.remove(id);
	}

	public Object getAction(String id) throws Exception
	{
		Action action = hasMapAction(id);
		if ( action == null )
			throw new NoActionRegistered("There are no actions with the id" + id);
		return (DynamicProxy.instantiate(action.getFunction()));
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
