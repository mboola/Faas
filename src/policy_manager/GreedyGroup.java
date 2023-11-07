package policy_manager;

import java.util.List;

import application.Invoker;
import faas_exceptions.NoInvokerAvaiable;

public class GreedyGroup implements PolicyManager{

	int	lastInvokerAssigned;

	public GreedyGroup () {
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

	private	Invoker getNextInvokerList(List<Invoker> invokers)
	{
		lastInvokerAssigned = updatePos(lastInvokerAssigned, invokers.size() - 1);
		return (invokers.get(lastInvokerAssigned));
		
	}

	@Override
	public Invoker getInvoker(List<Invoker> invokers, int ram) throws NoInvokerAvaiable {
		//conceptualment, aquest metode ha de omplir al maxim un invoker abans d-avan\ar al seguent
		//pero la llista d'invokers pot variar. o pot ser que tingui una referencia a un invoker
		//que esta omplint pero encara li queda i un altre que ja ha omplert es buida i te la mida
		//perfecta per executar la funcio que volem.
		//
		//hem de recorrer tota la llista d'invokers guardant l'index del que menys ram te, pero suficient
		//per a correr la funcio. seguidament, retornem l-invoker

		int		posLessRam;
		int		hasEnoughRam;
		long	lessRam;
		long	invRam;
		int		i;

		if (invokers.isEmpty())
			throw new NoInvokerAvaiable("List of invokers empty.");
		posLessRam = -1;
		lessRam = Long.MAX_VALUE;
		i = 0;
		hasEnoughRam = 0;
		for (Invoker invoker : invokers) {
			if (invoker.getMaxRam() >= ram)
			{
				hasEnoughRam = 1;
				invRam = invoker.getAvaiableRam();
				if (invRam >= ram && invRam < lessRam)
				{
					lessRam = invRam;
					posLessRam = i;
				}
			}
			i++;
		}
		//all invokers are full, distribute the invokers as a round robin
		if (posLessRam == -1 && hasEnoughRam == 1)
			return (getNextInvokerList(invokers));
		if (posLessRam == -1)
			throw new NoInvokerAvaiable("No Invoker Avaiable with at least " + ram + " RAM.");
		return (invokers.get(posLessRam));
	}
		
}
