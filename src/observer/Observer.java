package observer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import core.invoker.InvokerInterface;

public abstract class Observer implements ObserverInterface {

	protected InvokerInterface	invoker;
	protected String			id;

	public void initialize(String id, InvokerInterface invoker)	{
		this.invoker = invoker;
		this.id = id;
	}

	public void execution() {
	}

	public void update() {
	}
	
	protected <T> Optional<T> calculateMaxMetric(List<T> list, Comparator<T> comparator) {
		return list.stream().max(comparator);
	}

}
