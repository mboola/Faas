package decorator;

import java.util.HashMap;
import java.util.Map;

import faas_exceptions.NoResultAvailable;

public class Cache {

	private Map<String, Map<String, Object>> cacheDecorator;

	private static Cache	uniqueInstance = null;

	/**
	 * Checks if the Cache is instanciated, creates one if it isn't.
	 * 
	 * @return The Singleton instance of Cache. 
	 */
	public static Cache instantiate() {
		if (uniqueInstance == null)
			uniqueInstance = new Cache();
		return (uniqueInstance);
	}

	/**
	 * Constructs a new instance of Cache and instantiates all the structs it uses.
	 */
	private Cache() {
		cacheDecorator = new HashMap<>();
	}

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

    public <T, R> void cacheResult(String id, T args, R result) {
        String key = args.toString();
        Map<String, Object> innerMap = cacheDecorator.computeIfAbsent(id, k -> new HashMap<>());
        if (!innerMap.containsKey(key)) {
            innerMap.put(key, result);
        }
    }

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
