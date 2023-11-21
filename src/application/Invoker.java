package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import decorator.CacheDecorator;
import decorator.TimerDecorator;
import faas_exceptions.NoResultAvailable;
import observer.Observer;

public class Invoker {
	//TODO: delete this!! used to debug whats happening. Not final, must delete:
	private static int	numInvokers = 0;
	private String 		id;

	//not really sure but:
	private static Map<String, List<PairValues>> cacheDecorator = new HashMap<String, List<PairValues>>();

	/**
	 * Used to search the cache used by the cacheDecorator to see if there is
	 * a result stored in there
	 * @param args Data passed as an argument. Used to search result.
	 * @param id Identifier of the function used to 
	 * @return 
	 * @throws NoResultAvailable
	 */
	public static<T, R> R getResult(String id, T args) throws NoResultAvailable
	{
		List<PairValues> list = Invoker.cacheDecorator.get(id);
		if (list == null)
			throw new NoResultAvailable("");
		for (PairValues inputOutput : list) {
			//not sure this comparation is correct. I think this compares mem ref
			if (inputOutput.getArgs() == args)
				return ((R)inputOutput.getResult());
		}
		throw new NoResultAvailable("");
	}

	public static void printCache()
	{
		for (String key : Invoker.cacheDecorator.keySet()) {
			System.out.println("Function: " + key);
			for (PairValues pair : Invoker.cacheDecorator.get(key)) {
				System.out.println("Arg: " + pair.getArgs() + ". Ret: " + pair.getResult());
			}
		}
	}

	public static<T, R> void storeResult(String id, T args, R result)
	{
		//TODO maybe check if it exists?
		List<PairValues> list = Invoker.cacheDecorator.get(id);
		if (list == null)
		{
			list = new LinkedList<PairValues>();
			Invoker.cacheDecorator.put(id, list);
		}
		PairValues inputOutput = new PairValues(args, result);
		list.add(inputOutput);
	}

	//TODO: wtf is that above me???????????????
	//all good here
	private static final int 		MAX_THREADS = 8;

	private long					maxRam;
	private long					ramUsed;
	private ExecutorService 		executor;

	private static List<Observer>								observers = new LinkedList<Observer>();
	private static Function<Action, Function<Object, Object>>	decoratorInitializer = null;
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
	private Invoker(long ram)
	{
		this.maxRam = ram;
		ramUsed = 0;

		executor = Executors.newFixedThreadPool(MAX_THREADS);

		//TODO: delete this:
		id = ((Integer)numInvokers).toString();
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

	/**
	 * Adds an observer to the list of observers to be used by all invokers.
	 * 
	 * @param observer Observer that will be notified when a function is invoked.
	 */
	public static void setDecoratorInitializer(Function<Action, Function<Object, Object>> decoratorInitializer)
	{
		Invoker.decoratorInitializer = decoratorInitializer;
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
	public long	getAvaiableRam()
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
	private <T, R> Function<T, R> applyDecorators(Action action)
	{
		if (Invoker.decoratorInitializer == null)
			return ((Function<T, R>)action.getFunction());
		return ((Function<T, R>)Invoker.decoratorInitializer.apply(action));
	}

	/**
	 * This initializes the value of all the observers being used in the invocation.
	 * 
	 * @param id Id of the function. Needed by the observers to update the content of a dictionary in the controller.
	 * @return Map of metrics initialized. These metrics will be modified by 'notifyAllObservers' to create final metrics.
	 */
	private HashMap<Observer, Metric<Object>> initializeAllObservers(String id) 
	{
		HashMap<Observer, Metric<Object>> metrics = new HashMap<Observer, Metric<Object>>();

		for (Observer observer : observers) {
			metrics.put(observer, observer.initialize(id, Invoker.controller));
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
	private void notifyAllObservers(HashMap<Observer, Metric<Object>> metrics)
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
	@SuppressWarnings({"unchecked"})
	public <T, R> R invoke(Action action, T args, String id) throws Exception
	{
		Function<T, R>						functionDecorated;
		R									result;
		HashMap<Observer, Metric<Object>>	metricsList;

		metricsList = initializeAllObservers(id);

		functionDecorated = applyDecorators(action);

		//This breaks for some reason
		//System.out.println(functionDecorated.toString());
		result = functionDecorated.apply(args);

		notifyAllObservers(metricsList);
		return (result);
	}

	//TODO: javadoc this
	// This function tries to execute the function passed by parameter.
	// If there is no space in the pool, it waits and then it gets invoked.
	@SuppressWarnings({"unchecked"})
	public <T, R> Future<R> invokeAsync(Action action, T args, String id) throws Exception
	{
		Function<T, R>						functionDecorated;
		HashMap<Observer, Metric<Object>>	metricsList;
		Future<R>							futureResult;

		metricsList = initializeAllObservers(id);

		functionDecorated = applyDecorators(action);

		futureResult = executor.submit( 
			() -> {
				R	result;

				synchronized (this)
				{
					while (getAvaiableRam() - action.getRam() < 0)
					{
						try {
							wait();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					ramUsed += action.getRam();
				}
				result = functionDecorated.apply(args);
				synchronized (this)
				{
					ramUsed -= action.getRam();
					notify();
				}
				return (result);
			}
		);

		notifyAllObservers(metricsList);

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
