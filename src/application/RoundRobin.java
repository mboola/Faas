package application;
import java.util.List;

public class RoundRobin implements PolicyManager{

	private int	lastInvokerAssigned;

	public RoundRobin () {
		super();
		lastInvokerAssigned = 0;
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len)
			return (pos + 1);
		else
			return (0);
	}

	//TODO: change this implementation to one more robust!!!!!! what happens if--
	//TODO: --not all methods used in this invocation have the same time or if --
	//TODO: --a invoker is removed from the list
	@Override
	public Invoker getInvoker(List<Invoker> invokers, int ram) throws NoInvokerAvaiable
	{
		Invoker invoker;
		int	lastInvokerUsed;
		int	len;

		if (invokers.isEmpty())
			throw new NoInvokerAvaiable("No Invokers in list.");
		lastInvokerUsed = lastInvokerAssigned;
		len = invokers.size() - 1;
		lastInvokerAssigned = updatePos(lastInvokerAssigned, len);
		while (lastInvokerUsed != lastInvokerAssigned) {
			if (invokers.get(lastInvokerAssigned).getMaxRam() >= ram)
				break;
			lastInvokerAssigned = updatePos(lastInvokerAssigned, len);
		}
		invoker = invokers.get(lastInvokerAssigned);
		if (invoker.getMaxRam() >= ram)
			return (invoker);
		throw new NoInvokerAvaiable("No Invoker Avaiable with at least " + ram + " RAM.");
	}
    
}
