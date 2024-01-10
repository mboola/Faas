package decorator;

import java.util.function.Function;

/**
 * An abstract class representing a decorator for a function.
 * <p>
 * This class extends the Java {@link Function} class.
 * 
 * @param <T> The type of the input to the function.
 * @param <R> The type of the result of the function.
 */
public abstract class Decorator<T, R> implements Function<T, R> {

	protected Function<T, R> function;

	/**
     * Constructs a Decorator with the specified function to be decorated.
     *
     * @param function The function to be decorated.
     */
	public Decorator(Function<T, R> function) {
		this.function = function;
	}

	/**
     * Applies the decorated function to the given argument.
     *
     * @param t The argument to be passed to the decorated function.
	 * @return The result of applying the decorated function to the provided argument.
     */
	@Override
	public R apply(T t) {
		return (function.apply(t));
	}

}
