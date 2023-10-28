import java.util.List;

public class RoundRobin implements PolicyManager{

	private int	lastPosInvoker;

	public RoundRobin () {
		super();
		lastPosInvoker = 0;
	}

	private int updatePos(int pos, int len)
	{
		if (pos < len)
			return (pos + 1);
		else
			return (0);
	}

	@Override
	public Invoker getInvoker(List<Invoker> invokers, int ram) {
		Invoker invoker;
		int	firstElement;
		int	len;
		
		firstElement = lastPosInvoker;
		len = invokers.size() - 1;
		lastPosInvoker = updatePos(lastPosInvoker, len);
		while (firstElement != lastPosInvoker) {
			if (invokers.get(lastPosInvoker).getMaxRam() >= ram)
				break;
			lastPosInvoker = updatePos(lastPosInvoker, len);
		}
		invoker = invokers.get(lastPosInvoker);
		if (invoker.getMaxRam() >= ram)
			return (invoker);
		return (null);
	}
    
}
