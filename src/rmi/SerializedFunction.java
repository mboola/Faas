package rmi;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A serializable version of the {@link java.util.function.Function} interface.
 * Allows the representation of a function that can be serialized and transmitted
 * over a network, typically for Remote Method Invocation (RMI) purposes.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 * @see Function
 * @see Serializable
 */
public interface SerializedFunction<T, R> extends Function<T, R>, Serializable {}
