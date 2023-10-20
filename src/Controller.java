import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Controller<T, R> {
	private List<Invoker> invokers = new LinkedList<Invoker>();
	private Map<String, Function<T, R>> actions = new HashMap<String, Function<T, R>>();
    
	public static Controller instantiate()
	{
		if (unicInstance == null)
			unicInstance = new Controller<>();
		return (unicInstance);
	}
	protected Controller() {
	}
	private static Controller unicInstance = null;

	/* Here we will call a function from the proxy */
	private String GetId(Action<T, R> action)
	{
		return ("hola:)");
	}

	/* Used to search if we already have this action in our map */
	private boolean MapHasAction(String id)
	{
		if ( actions.isEmpty() )
			return (false);
		if ( actions.get(id) == null )
			return (false);
		return (true);
	}

	public void RegisterAction(String id, Function<T, R> f)
	{
		//String	id;

		//id = GetId(action);
		if ( !MapHasAction(id) )
		{
			actions.put(id, f);
			return ;
		}
		//throw error. already exists
	}

	public void	ListActions()
	{
		if ( actions.isEmpty())
		{
			//nothing to show
			return ;
		}
		for(String key : actions.keySet())
			System.out.println(key);
	}

	public R InvokeAction(String id, T args) throws Exception
	{
		Function<T, R>	action;

		if ( !MapHasAction(id) )
		{
			//error, we dont have this action in our map
			System.out.println("Error");
			return (null);
		}
		action = actions.get(id);
		return (action.apply(args));
	}

	public List<R> InvokeAction(String id, List<T> args) throws Exception
	{
		Function<T, R>	action;
		List<R> result;

		if ( !MapHasAction(id) )
		{
			//error, we dont have this action in our map
			System.out.println("Error");
			return (null);
		}
		result = new LinkedList<R>();
		action = actions.get(id);
		for (T element : args)
			result.add(action.apply(element));
		return (result);
	}

	public void RemoveAction(String id)
	{
		if ( !MapHasAction(id) )
		{
			//error, we dont have this action in our map
			return ;
		}
		actions.remove(id);
	}
}
