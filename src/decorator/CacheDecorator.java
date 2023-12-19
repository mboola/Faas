package decorator;
import java.util.function.Function;

import faas_exceptions.NoResultAvailable;

/**
 * A decorator class for storing a cache of functions executed. 
 */
public class CacheDecorator<T, R> extends Decorator<T, R>{

	private String	id;

	/**
     * Constructs a new CacheDecorator with the specified function and identifier.
     *
     * @param function The function to be decorated and cached.
     * @param id       The identifier for the caching mechanism.
     */
	public CacheDecorator(Function<T, R> function, String id) {
		super(function);
		this.id = id;
	}

	/**
     * Applies the cached result or invokes the function and caches the result if not found in the cache.
     *
     * @param t The input to the function.
     * @return The result of the function.
     */
	@Override
	public R apply(T t){
		R		result;

		Cache cache = Cache.instantiate();
		try {
			result = cache.getCacheResult(id, t);
		} //I do this in case a function is suposed to return null as a valid result
		catch (NoResultAvailable e1) {
			result = getFunction().apply(t);
			cache.cacheResult(id, t, result);
		}
		return (result);
	}

}
