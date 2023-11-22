package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import RMI.InvokerInterface;
import dynamic_proxy.DynamicProxy;
import faas_exceptions.NoActionRegistered;
import policy_manager.PolicyManager;

public class Controller {
	private Map<String, Action>	actions;
	public MetricSet			metrics;
	private List<InvokerInterface>		invokers;
	private PolicyManager		policyManager;

	public static Controller instantiate() {
		if (unicInstance == null)
			unicInstance = new Controller();
		return (unicInstance);
	}
	protected Controller() {
		invokers = new LinkedList<InvokerInterface>();
		actions = new HashMap<String, Action>();
		metrics = new MetricSet();
	}
	private static Controller unicInstance = null;

	public void registerInvoker(InvokerInterface invoker)
	{
		//TODO: if invoker is already at list throw error
		invokers.add(invoker);
	}

	public void deleteInvoker(Invoker invoker)
	{
		//TODO: if invoker is not in the list throw error
		invokers.remove(invoker);
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
