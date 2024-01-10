package core.application;

import java.util.function.Function;

/**
 * Represents an action that takes an argument of type {@code T} and produces a result of type {@code R}.
 * This interface extends the {@link Function} interface and declares the {@code apply} method, which
 * represents the action to be performed.
 *
 * @param <T> the type of the input to the action
 * @param <R> the type of the result of the action
 *
 * @see Function
 */
public interface Action<T, R> extends Function<T, R> {

	/**
     * Applies this function to the given argument.
     *
     * @param arg the function argument
     * @return the function result
     */
	R apply(T arg);
}
