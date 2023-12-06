package policy_manager;
import java.util.List;

import faas_exceptions.NoInvokerAvailable;
import invoker.InvokerInterface;

public class RoundRobin implements PolicyManager{

	private int	lastInvokerAssigned;

	public RoundRobin () {
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
	//TODO: --not all methods used in this invocation have the same time (i dont care) or if --
	//TODO: --a invoker is removed from the list while executing
	@Override
	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws Exception
	{
		InvokerInterface invoker;
		int	lastInvokerUsed;
		int	len;

		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		lastInvokerUsed = lastInvokerAssigned;
		len = invokers.size() - 1;
		lastInvokerAssigned = updatePos(lastInvokerAssigned, len);
		while (lastInvokerUsed != lastInvokerAssigned) {
			//if (invokers.get(lastInvokerAssigned).canExecute(ram))
			if (invokers.get(lastInvokerAssigned).getMaxRam() >= ram)
				break;
			lastInvokerAssigned = updatePos(lastInvokerAssigned, len);
		}
		invoker = invokers.get(lastInvokerAssigned);
		if (invoker.getMaxRam() >= ram)
			return (invoker);
		throw new NoInvokerAvailable("No Invoker Available with at least " + ram + " RAM.");
	}

	@Override
	public PolicyManager copy() {
		return (new RoundRobin());
	}
    
}
