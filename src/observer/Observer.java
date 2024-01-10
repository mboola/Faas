package observer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.ToDoubleFunction;

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
	
	protected <T> T calculateMaxMetric(List<T> list, Comparator<T> comparator) {
		Optional<T> max  = list.stream().max(comparator);
		if (max.isPresent()) return max.get();
		else return null;
	}

	protected <T> T calculateMinMetric(List<T> list, Comparator<T> comparator) {
		Optional<T> max  = list.stream().min(comparator);
		if (max.isPresent()) return max.get();
		else return null;
	}

	protected <T> double calculateAverageMetric(List<T> list, ToDoubleFunction<T> mapperFunction) {
		return list.stream()
				.mapToDouble(mapperFunction)
				.average()
				.orElse(0.0);
	}

	protected <T> T calculateAccumulativeMetric(List<T> list, T identity, BinaryOperator<T> accumulator) {
		return list.stream()
				.reduce(identity, accumulator);
	}

}
