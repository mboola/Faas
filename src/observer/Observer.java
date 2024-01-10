package observer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.ToDoubleFunction;

import core.invoker.InvokerInterface;

/**
 * Abstract base class for creating observers that monitor the behavior of an {@link InvokerInterface}.
 * Observers can capture and analyze metrics associated with the invoker's execution.
 * <p>
 * This observer implements the {@link observer.ObserverInterface} interface.
 */
public abstract class Observer implements ObserverInterface {

	protected InvokerInterface	invoker;
	protected String			functionId;

	/**
	 * Initializes the observer with a function identifier of the function which invokation will observe
	 * and the associated invoker.
	 * Called when PolicyManger selects the invoker to invoke a function.
	 *
	 * @param functionId The function identifier of the function to be invoked by the Invoker.
	 * @param invoker	 The Invoker to be observed.
	 */
	public void initialize(String functionId, InvokerInterface invoker)	{
		this.invoker = invoker;
		this.functionId = functionId;
	}

	/**
	 * Method to be overridden by subclasses for any pre-execution actions.
	 * Called when the Invoker selects the invoker to invoke a function.
	 */
	public void execution() {
	}

	/**
	 * Method to be overridden by subclasses for any post-execution actions.
	 * Called when the Invoker finishes the invocation.
	 */
	public void update() {
	}
	
	/**
	 * Calculates the maximum value in the provided list using the specified comparator.
	 *
	 * @param list       The list of values.
	 * @param comparator The comparator to determine the maximum.
	 * @param <T>        The type of elements in the list.
	 * @return The maximum value in the list, or null if the list is empty.
	 */
	protected <T> T calculateMaxMetric(List<T> list, Comparator<T> comparator) {
		Optional<T> max  = list.stream().max(comparator);
		if (max.isPresent()) return max.get();
		else return null;
	}

	/**
	 * Calculates the minimum value in the provided list using the specified comparator.
	 *
	 * @param list       The list of values.
	 * @param comparator The comparator to determine the minimum.
	 * @param <T>        The type of elements in the list.
	 * @return The minimum value in the list, or null if the list is empty.
	 */
	protected <T> T calculateMinMetric(List<T> list, Comparator<T> comparator) {
		Optional<T> max  = list.stream().min(comparator);
		if (max.isPresent()) return max.get();
		else return null;
	}

	/**
	 * Calculates the average value of the provided list using the specified mapper function.
	 *
	 * @param list           The list of values.
	 * @param mapperFunction The function to map elements to double values.
	 * @param <T>            The type of elements in the list.
	 * @return The average value of the list or 0 if the list is empty.
	 */
	protected <T> double calculateAverageMetric(List<T> list, ToDoubleFunction<T> mapperFunction) {
		return list.stream()
				.mapToDouble(mapperFunction)
				.average()
				.orElse(0.0);
	}

	/**
	 * Calculates the cumulative value of the provided list using the specified binary operator.
	 *
	 * @param list      The list of values.
	 * @param identity  The identity value for the accumulator.
	 * @param accumulator The binary operator to apply to the elements in the list.
	 * @param <T>       The type of elements in the list.
	 * @return The cumulative value of the list.
	 */
	protected <T> T calculateAccumulativeMetric(List<T> list, T identity, BinaryOperator<T> accumulator) {
		return list.stream()
				.reduce(identity, accumulator);
	}

}
