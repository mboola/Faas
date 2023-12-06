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

	@Override
	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws Exception
	{
		InvokerInterface invoker;
		int	lastInvokerChecked;
		int	len;

		//if there are no invokers we cant select them
		if (invokers.isEmpty()) throw new NoInvokerAvailable("No Invokers in list.");
		lastInvokerChecked = lastInvokerAssigned;
		len = invokers.size() - 1;
		lastInvokerChecked = updatePos(lastInvokerChecked, len);
		invoker = null;
		while (lastInvokerAssigned != lastInvokerChecked) {
			//this selects an invoker from this invoker that can execute the invokable
			invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
			if (invoker != null)
				break;
			lastInvokerChecked = updatePos(lastInvokerChecked, len);
		}
		lastInvokerAssigned = lastInvokerChecked;
		if (invoker == null)
			invoker = invokers.get(lastInvokerChecked).selectInvoker(ram);
		if (invoker == null) throw new NoInvokerAvailable("No Invoker Available with at least " + ram + " RAM.");
		return (invoker);
	}

	@Override
	public PolicyManager copy() {
		return (new RoundRobin());
	}
    
}
