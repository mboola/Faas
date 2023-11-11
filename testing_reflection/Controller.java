//THIS IS A CONCEPTUAL VERSION, NOT A FUNCTIONAL ONE

import java.util.Map;
import java.util.concurrent.Future;

import application.Action;

public class Controller implements ActionProxy{
	private Map<String, Action<Integer, Object>>	actions;

	public static Controller instantiate() {
		if (unicInstance == null)
			unicInstance = new Controller();
		return (unicInstance);
	}
	protected Controller() {
		//initialize values of this class
	}
	private static Controller unicInstance = null;

	//used the register an Action in the controller. Ram must be inputed in MegaBytes
	public void registerAction(String id, Object f, int ram)
	{
		actions.put(id, new Action<Integer, Object>(ram, f));
	}

	public void removeAction(String id)
	{
		actions.remove(id);
	}

	public void	listActions()
	{
		//TODO: is this all the info I wanna show?
		if ( actions.isEmpty())
			return ;
		for(String key : actions.keySet())
			System.out.println(key);
	}

	@Override
	public <T, R> R invoke(String id, T args) throws Exception
	{
		//invoke action from the invokers
		Action<Integer, Object> action = actions.get(id);
		return (action.apply(args));
	}
}
