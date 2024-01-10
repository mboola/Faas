package decorator;

import java.util.function.Function;

import core.exceptions.NoResultAvailable;

/**
 * A decorator that adds caching functionality to a given function.
 * <p>
 * This decorator extends the {@link decorator.Decorator} class.
 *
 * @param <T> The type of the input to the function.
 * @param <R> The type of the result of the function.
 */
public class CacheDecorator<T, R> extends Decorator<T, R>{

	private String	id;

	/**
	 * Constructs a new CacheDecorator with the specified function and identifier.
	 *
	 * @param function The function to be decorated.
	 * @param id       The identifier for the caching mechanism.
	 */
	public CacheDecorator(Function<T, R> function, String id) {
		super(function);
		this.id = id;
	}

	/**
	 * Applies caching to the decorated function. If the result is not found in the cache, the function is invoked,
	 * and the result is stored in the cache for future use.
	 *
	 * @param t The input to the function.
	 * @return The result from the cache or from the invocation.
	 */
	@Override
	public R apply(T t){
		R	result;

		Cache cache = Cache.instantiate();
		try {
			// Attempt to retrieve result from cache
			result = cache.getCacheResult(id, t);
		}
		catch (NoResultAvailable e) {
			// If result is not found in cache, invoke the function and cache the result
			result = function.apply(t);
			cache.cacheResult(id, t, result);
		}
		return (result);
	}

}
