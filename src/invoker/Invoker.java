package invoker;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import application.Controller;
import application.Invokable;
import application.Metric;
import faas_exceptions.NoInvokerAvailable;
import faas_exceptions.NoResultAvailable;
import observer.Observer;
import policy_manager.PolicyManager;

public class Invoker implements InvokerInterface {

	private static Map<String, Map<String, Object>> cacheDecorator = new HashMap<>();

    public static void printCache() {
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

    public static <T, R> void cacheResult(String id, T args, R result) {
        String key = args.toString();
        Map<String, Object> innerMap = cacheDecorator.computeIfAbsent(id, k -> new HashMap<>());
        if (!innerMap.containsKey(key)) {
            innerMap.put(key, result);
        }
    }

    public static <T, R> R getCacheResult(String id, T args) throws NoResultAvailable {
        String key = args.toString();
        Map<String, Object> innerMap = cacheDecorator.get(id);
        if (innerMap == null || !innerMap.containsKey(key)) {
            throw new NoResultAvailable("No matching arguments have been found");
        }
        return (R) innerMap.get(key);
    }

	//all good here
	private static final int 		MAX_THREADS = 8;
	private static long				numInvokers = 0;

	private String 					id;
	private long					maxRam;
	private long					ramUsed;
	private ExecutorService 		executor;

	private static List<Observer>								observers = new LinkedList<Observer>();
	private static Function<Invokable, Function<Object, Object>>	decoratorInitializer = null;
	private static Controller									controller = null;
	
	/**
	 * This method creates an Invoker.
	 * 
	 * @param ram The max ram of the Invoker.
	 * @return The Invoker created, or null if ram is lesser or equal to zero.
	 */
	public static Invoker createInvoker(long ram)
	{
		if (ram <= 0)
			return (null);
		else
			return (new Invoker(ram));
	}

	/**
	 * Constructs a new instance of Invoker.
	 * 
	 * @param ram The max ram of the Invoker.
	 * 
	 * <p><strong>Note:</strong> Constuctor will only be called from 'createInvoker' to ensure no Invokers with invalid parameters are created.</p>
	 */
	protected Invoker(long ram)
	{
		this.maxRam = ram;
		ramUsed = 0;

		executor = Executors.newFixedThreadPool(MAX_THREADS);

		id = ((Long)numInvokers).toString();
		numInvokers++;
	}

	/**
	 * Assigns a Controller to the static variable controller of this class.
	 * 
	 * @param controller The Controller to be used by all the invokers.
	 */
	public static void setController(Controller controller)
	{
		Invoker.controller = controller;
	}

	/**
	 * Adds an observer to the list of observers to be used by all invokers.
	 * 
	 * @param observer Observer that will be notified when a function is invoked.
	 */
	public static void addObserver(Observer observer)
	{
		//TODO: check if the observer is already added
		Invoker.observers.add(observer);
	}

	/**
	 * Removes an observer to the list of observers to be used by all invokers.
	 * 
	 * @param observer Observer that won't be notified when a function is invoked.
	 */
	public static void removeObserver(Observer observer)
	{
		//TODO: check of the observer is on the list
		Invoker.observers.remove(observer);
	}

	
	public static void setDecoratorInitializer(Function<Invokable, Function<Object, Object>> decoratorInitializer)
	{
		Invoker.decoratorInitializer = decoratorInitializer;
	}

	public String getId()
	{
		return (id);
	}

	/**
	 * This selects an invoker to invoke a function.
	 */
	public Invoker getInvoker()
	{
		return (this);
	}

	/**
	 * Method used to select a invoker to execute a function based on the ram it consumes and the policy we have assigned.
	 * @param ram
	 * @return
	 * @throws Exception <p>The exception can be caused because:</p>
	 * <ul>
	 * 	<li>NoPolicyManagerRegistered: There is no policyManager registered.</li>
	 * 	<li>NoInvokerAvailable: There is no invoker with enough max ram to execute the invokable.</li>
	 *  <li>Exeption: something goes wrong with RMI.</li>
	 * </ul>
	 */
	@Override
	public InvokerInterface selectInvoker(long ram) throws Exception
	{
		if (this.maxRam < ram) throw new NoInvokerAvailable("Not enough ram to assign this invoker.");
		return (this);
	}

	public void	setPolicyManager(PolicyManager policyManager)
	{
	}

	/**
	 * Getter of the ram being used.
	 * 
	 * @return Ram being used.
	 */
	public long	getRamUsed()
	{
		return (ramUsed);
	}

	/**
	 * Getter of the ram avaiable to use.
	 * 
	 * @return Ram avaiable to use.
	 */
	public long	getAvailableRam()
	{
		return (maxRam - ramUsed);
	}
	
	/**
	 * Getter of the max ram of the Invoker.
	 * 
	 * @return Max ram to be used by this invoker.
	 */
	public long	getMaxRam()
	{
		return (maxRam);
	}

	//TODO: javadocs
	//TODO specify when I wanna use this Decorators. Not always are needed. Specially in testing
	@SuppressWarnings({"unchecked"})
	private <T, R> Function<T, R> applyDecorators(Invokable invokable)
	{
		if (Invoker.decoratorInitializer == null)
			return ((Function<T, R>)invokable.getInvokable());
		return ((Function<T, R>)Invoker.decoratorInitializer.apply(invokable));
	}

	private void preinitializeObservers(String id) throws Exception 
	{
		for (Observer observer : observers) {
			observer.preinitialize(id, Invoker.controller, this);
		}
	}

	/**
	 * This initializes the value of all the observers being used in the invocation.
	 * 
	 * @param id Id of the function. Needed by the observers to update the content of a dictionary in the controller.
	 * @return Map of metrics initialized. These metrics will be modified by 'notifyAllObservers' to create final metrics.
	 * @throws Exception
	 */
	private HashMap<Observer, Metric<Object>> initializeObservers(String id) throws Exception 
	{
		HashMap<Observer, Metric<Object>> metrics = new HashMap<Observer, Metric<Object>>();

		for (Observer observer : observers) {
			metrics.put(observer, observer.initialize(id, Invoker.controller, this));
		}
		return (metrics);
	}

	/**
	 * This modifies all the values of the metrics created by 'initializeAllObservers'
	 * 
	 * @param metrics Map of all the metrics to be updated by the observers
	 * 
	 * <p><strong>Note:</strong> If the list of observers changed between initialization and this method,
	 * two things can happen:
	 * <ul>
	 * 		<li> If observers were added, the new observers will not be notified.</li>
	 * 		<li> If observers were removed, the removed observers will not be notified.</li>
	 * </ul>
	 * This is to ensure a correct funcionality of the observers.
	 * </p>
	 */
	private void notifyObservers(HashMap<Observer, Metric<Object>> metrics)
	{
		Metric<Object>	metric;

		for (Observer observer : observers) {
			metric = metrics.get(observer);
			if (metric != null)
				observer.update(metric);
		}
	}

	/**
	 * This method is called to execute a sync function passed by reference, applying all the observers and decorators.
	 * 
	 * @param action Function to be executed and the ram it consumes.
	 * @param args Arguments needed by the function, of type T
	 * @param id Identifier of the function. Needed by the observers and decorators to store data correctly.
	 * @return The result of the function invoked, of type R
	 * @throws Exception //TODO: i dont remember
	 */
	public <T, R> R invoke(Invokable invokable, T args, String id) throws Exception
	{
		Function<T, R>						functionDecorated;
		R									result;
		HashMap<Observer, Metric<Object>>	metricsList;

		preinitializeObservers(id);

		metricsList = initializeObservers(id);

		functionDecorated = applyDecorators(invokable);

		result = functionDecorated.apply(args);

		notifyObservers(metricsList);
		return (result);
	}

	//TODO: javadoc this
	// This function tries to execute the function passed by parameter.
	// If there is no space in the pool, it waits and then it gets invoked.
	public <T, R> Future<R> invokeAsync(Invokable invokable, T args, String id) throws Exception
	{
		Function<T, R>	functionDecorated;
		Future<R>		futureResult;

		functionDecorated = applyDecorators(invokable);

		preinitializeObservers(id);

		futureResult = executor.submit( 
			() -> {
				R	result;
				HashMap<Observer, Metric<Object>>	metricsList;

				synchronized (this)
				{
					while (getAvailableRam() - invokable.getRam() < 0)
					{
						try {
							wait();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					ramUsed += invokable.getRam();
					metricsList = initializeObservers(id);
				}
				result = functionDecorated.apply(args);
				synchronized (this)
				{
					ramUsed -= invokable.getRam();
					notifyObservers(metricsList);
					notify();
				}
				return (result);
			}
		);
		return (futureResult);
	}

	/**
	 * This shuts down the executor of the Invoker. Must be called when the application finishes.
	 */
	public void shutdownInvoker()
	{
		executor.shutdown();
	}
}
