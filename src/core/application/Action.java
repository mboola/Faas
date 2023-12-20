package core.application;

import java.util.function.Function;

public interface Action<T, R> extends Function<T, R> {
	R apply(T arg);
}
