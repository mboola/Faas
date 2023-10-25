import java.util.List;

public class RoundRobin implements PolicyManager{

	private int	lastInvoker;

	public RoundRobin ()
	{
		super();
		lastInvoker = 0;
	}

	@Override
	public Invoker getInvoker(List<Invoker> invokers, int ram) {
		Invoker invoker;
		
		if (lastInvoker < invokers.size() - 1)
			lastInvoker++;
		else
			lastInvoker = 0;
		invoker = invokers.get(lastInvoker);
		return (invoker);
	}
    
}
