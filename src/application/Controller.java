package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Controller {
	private Map<String, Action<Integer, Object>>	actions;
	public MetricSet								metrics;
	private List<Invoker>							invokers;
	private PolicyManager							policyManager;

	public static Controller instantiate() {
		if (unicInstance == null)
			unicInstance = new Controller();
		return (unicInstance);
	}
	protected Controller() {
		invokers = new LinkedList<Invoker>();
		actions = new HashMap<String, Action<Integer, Object>>();
		metrics = new MetricSet();
	}
	private static Controller unicInstance = null;

	/* Here we will call a function from the proxy */
	private String GetId(Function<Object, Object> action)
	{
		//TODO: implement this
		return (action.toString());
	}

	public void registerInvoker(Invoker invoker)
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
	private boolean hasMapAction(String id)
	{
		if ( actions.isEmpty() )
			return (false);
		if ( actions.get(id) == null )
			return (false);
		return (true);
	}

	//used the register an Action in the controller. Ram must be inputed in MegaBytes
	public void registerAction(String id, Object f, int ram)
	{
		//String	id;

		//id = GetId(action);
		if ( !hasMapAction(id) )
		{
			actions.put(id, new Action<Integer, Object>(ram, f));
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

	private Invoker selectInvoker(int ram) throws Exception
	{
		return (policyManager.getInvoker(invokers, ram));
	}

	private <T, R> R getResult(Action<Integer, Object> action, T args, String id) throws Exception
	{
		Invoker	invoker;

		invoker = selectInvoker(action.getRam());
		return (selectInvoker(action.getRam()).invoke(action, args, id));
	}

	public <T, R> R invoke(String id, T args) throws Exception
	{
		Action<Integer, Object>	action;

		if ( !hasMapAction(id) )
		{
			//TODO: throw error, we dont have this action in our map
			System.out.println("Error");
			return (null);
		}
		action = actions.get(id);
		return (getResult(action, args, id));
	}

	public <T, R> List<R> invoke(String id, List<T> args) throws Exception
	{
		Action<Integer, Object>	action;
		List<R> 				result;

		if ( !hasMapAction(id) )
		{
			//TODO: throw error, we dont have this action in our map
			System.out.println("Error");
			return (null);
		}
		result = new LinkedList<R>();
		action = actions.get(id);
		for (T element : args)
			result.add(getResult(action, element, id));
		return (result);
	}

	public <T, R> Future<R> invoke_async(String id, T args) throws Exception
	{
		Action<Integer, Object>	action;

		action = actions.get(id);
		return (selectInvoker(action.getRam()).invokeAsync(action, args));
	}

	public void removeAction(String id)
	{
		if ( !hasMapAction(id) )
		{
			//TODO: throw error, we dont have this action in our map
			return ;
		}
		actions.remove(id);
	}

	public void shutdownAllInvokers()
	{
		for (Invoker invoker:invokers) {
			invoker.shutdownInvoker();
		}
	}
}
