import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Controller {
	private List<Invoker> invokers;
	private Map<String, Object> actions;

	public static Controller instantiate()
	{
		if (unicInstance == null)
			unicInstance = new Controller();
		return (unicInstance);
	}
	protected Controller() {
		invokers = new LinkedList<Invoker>();
		actions = new HashMap<String, Object>();
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

	//this will be police manager
	private Invoker selectInvoker()
	{
		Invoker invoker;

		invoker = invokers.get(0);
		return (invoker);
	}

	public void registerAction(String id, Object f)
	{
		//String	id;

		//id = GetId(action);
		if ( !hasMapAction(id) )
		{
			actions.put(id, f);
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

	public <T, R> R invoke(String id, T args) throws Exception
	{
		Function<T, R>	action;
		Invoker			invoker;

		if ( !hasMapAction(id) )
		{
			//TODO: throw error, we dont have this action in our map
			System.out.println("Error");
			return (null);
		}
		action = (Function<T, R>)actions.get(id);
		invoker = selectInvoker();
		return (invoker.invoke(action, args));
	}

	public <T, R> List<R> invoke(String id, List<T> args) throws Exception
	{
		Function<T, R>	action;
		List<R> 		result;
		Invoker			invoker;

		if ( !hasMapAction(id) )
		{
			//TODO: throw error, we dont have this action in our map
			System.out.println("Error");
			return (null);
		}
		result = new LinkedList<R>();
		action = (Function<T, R>)actions.get(id);
		for (T element : args)
		{
			invoker = selectInvoker();
			result.add(invoker.invoke(action, element));
		}
		return (result);
	}

	public <T, R> Future<R> invoke_async(String id, T args) throws Exception
	{
		Function<T, R>	action;
		Invoker			invoker;

		action = (Function<T, R>)actions.get(id);
		invoker = selectInvoker();
		return (invoker.invokeAsync(action, args));
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
