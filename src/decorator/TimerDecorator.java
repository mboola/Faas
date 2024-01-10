package decorator;

import java.util.function.Function;

/**
 * A decorator class that measures the execution time of a function.
 * <p>
 * This decorator extends the {@link decorator.Decorator} class.
 * 
 * @param <T> The type of the input to the function.
 * @param <R> The type of the result of the function.
 */
public class TimerDecorator<T, R> extends Decorator<T, R>{

	/**
	 * Constructs a TimerDecorator with the specified function to be decorated.
	 *
	 * @param function The function to be decorated.
	 */
	public TimerDecorator(Function<T, R> function) {
		super(function);
	}

	/**
	 * Applies the timer decorator logic to the given argument, measuring the execution time of the underlying function.
	 *
	 * @param t The argument to be passed to the decorated function.
	 * @return The result of the invocation of the decorated function.
	 */
	@Override
	public R apply(T t) {
		long	timeExecution;
		R		result;

		timeExecution = System.nanoTime();
		result = function.apply(t);
		timeExecution = System.nanoTime() - timeExecution;
		System.out.println("Time of execution is " + timeExecution + " ns.");
		return (result);
	}

}
