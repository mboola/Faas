package application;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Invoker {
	//TODO: delete this!! used to debug whats happening. Not final, must delete:
	private static int	numInvokers = 0;
	private String 		id;

	//not really sure but:
	private static List<Observer>	observers = new LinkedList<Observer>();
	private static Controller		controller;
	private static Map<String, List<PairValues>> cacheDecorator = new HashMap<String, List<PairValues>>();

	/**
	 * Used to search the cache used by the cacheDecorator to see if there is
	 * a result stored in there
	 * @param args Data passed as an argument. Used to search result.
	 * @param id Identifier of the function used to 
	 * @return 
	 * @throws NoResultAvaiable
	 */
	public static<T, R> R getResult(String id, T args) throws NoResultAvaiable
	{
		List<PairValues> list = Invoker.cacheDecorator.get(id);
		if (list == null)
			throw new NoResultAvaiable("");
		for (PairValues inputOutput : list) {
			//not sure this comparation is correct. I think this compares mem ref
			if (inputOutput.getArgs() == args)
				return ((R)inputOutput.getResult());
		}
		throw new NoResultAvaiable("");
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

	//all good here
	private static final int 	MAX_THREADS = 8;
	private long				maxRam;
	private long				ramUsed;
	private ExecutorService 	executor;

	
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

	public static void setController(Controller controller)
	{
		Invoker.controller = controller;
	}

	public static void addObserver(Observer observer)
	{
		Invoker.observers.add(observer);
	}

	public long	getRamUsed()
	{
		return (ramUsed);
	}

	public long	getAvaiableRam()
	{
		return (maxRam - ramUsed);
	}

	public long	getMaxRam()
	{
		return (maxRam);
	}

	private <T, R> Function<T, R> applyDecorators(Function<T, R> function, String id)
	{
		Function<T, R>	timerDecorator;
		Function<T, R>	cacheDecorator;

		cacheDecorator = new CacheDecorator<>(function, id);
		timerDecorator = new TimerDecorator<>(cacheDecorator);

		return (timerDecorator);
	}

	private List<Metric<Object>> initializeAllObservers(String id) 
	{
		List<Metric<Object>> metrics = new LinkedList<Metric<Object>>();

		for (Observer observer : observers) {
			metrics.add(observer.initialize(id, Invoker.controller));
		}
		return (metrics);
	}

	private void notifyAllObservers(List<Metric<Object>> metrics)
	{
		int	metricIndex = 0;

		//if (metrics.count() != observers.count())
		//throw error or something

		for (Observer observer : observers) {
			observer.update(metrics.get(metricIndex));
			metricIndex++;
		}
	}

	public <T, R> R invoke(Action<Integer, Object> action, T args, String id) throws Exception
	{
		Function<T, R>			function;
		Function<T, R>			functionDecorated;
		R						result;
		List<Metric<Object>>	metricsList;

		metricsList = initializeAllObservers(id);
		function = (Function<T, R>) action.getFunction();

		functionDecorated = applyDecorators(function, id);

		result = functionDecorated.apply(args);
		notifyAllObservers(metricsList);
		return (result);
	}

	// This function tries to execute the function passed by parameter.
	// If there is no space in the pool, it waits and then it gets invoked.
	public <T, R> Future<R> invokeAsync(Action<Integer, Object> action, T args) throws Exception
	{
		Function<T, R>	function;
		
		function = (Function<T, R>) action.getFunction();
		return (executor.submit( 
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
				result = function.apply(args);
				synchronized (this)
				{
					ramUsed -= action.getRam();
					notify();
				}
				return (result);
			}
		));
	}

	public void shutdownInvoker()
	{
		executor.shutdown();
	}
}
