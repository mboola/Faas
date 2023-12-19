package decorator;

import java.util.HashMap;
import java.util.Map;

import faas_exceptions.NoResultAvailable;

/**
 * A class that acts as the cache itself of the cache decorator.
 */
public class Cache {

	private Map<String, Map<String, Object>> cacheDecorator;

	private static Object mutex = new Object();
	private static Cache	uniqueInstance = null;

	/**
	 * Checks if the Cache is instanciated, creates one if it isn't.
	 * This method is thread safe.
	* 
	 * @return The Singleton instance of Cache. 
	 */
	public static Cache instantiate() {
		Cache instance;

		instance = uniqueInstance;
		if (uniqueInstance == null)
		{
			synchronized (mutex)
			{
				instance = uniqueInstance;
				if (instance == null)
					instance = uniqueInstance = new Cache();
			}
		}
		return (instance);
	}

	/**
	 * Constructs a new instance of Cache and instantiates all the structs it uses.
	 */
	private Cache() {
		cacheDecorator = new HashMap<>();
	}

	/**
	 * Prints the contents of the cache decorator. If the cache is empty, a corresponding message is displayed.
	 */
	public void printCache() {
		if (cacheDecorator.isEmpty()) {
			System.out.println("Cache is empty.");
			return;
		}
		for (String id : cacheDecorator.keySet()) {
			System.out.println("Function: " + id);
			Map<String, Object> innerMap = cacheDecorator.get(id);
			for (Map.Entry<String, Object> entry : innerMap.entrySet()) {
				System.out.println("Args: " + entry.getKey() + ". Ret: " + entry.getValue());
			}
		}
	}

	/**
	 * Caches the result of a function call based on the provided id, arguments, and result.
     *
     * @param id     The identifier for the function.
     * @param args   The arguments used in the function call.
     * @param result The result of the function call.
     * @param <T>    The type of the arguments.
     * @param <R>    The type of the result.
     */
    public <T, R> void cacheResult(String id, T args, R result) {
        String key = args.toString();
        Map<String, Object> innerMap = cacheDecorator.computeIfAbsent(id, k -> new HashMap<>());
        if (!innerMap.containsKey(key)) {
            innerMap.put(key, result);
        }
    }

    /**
     * Retrieves the cached result of a function call based on the provided id and arguments.
     *
     * @param id   The identifier for the function.
     * @param args The arguments used in the function call.
     * @param <T>  The type of the arguments.
     * @param <R>  The type of the result.
     * @return The cached result.
     * @throws NoResultAvailable If no matching arguments have been found in the cache.
     */
	@SuppressWarnings({"unchecked"})
    public <T, R> R getCacheResult(String id, T args) throws NoResultAvailable {
        String key = args.toString();
        Map<String, Object> innerMap = cacheDecorator.get(id);
        if (innerMap == null || !innerMap.containsKey(key)) {
            throw new NoResultAvailable("No matching arguments have been found");
        }
        return (R) innerMap.get(key);
    }

}
