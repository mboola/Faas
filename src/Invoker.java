import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import javax.script.Invocable;

public class Invoker {
	//TODO: delete this!! used to debug whats happening. Not final, must delete:
	private static int	numInvokers = 0;
	private String 		id;

	//not really sure but:
	private static List<Observer>	observers = new LinkedList<Observer>();
	private static Controller		controller;

	//all good here
	private static final int 	MAX_THREADS = 8;
	private long				maxRam;
	private long				ramUsed;
	private ExecutorService 	executor;

	private final Lock			asyncLock;
	private final Condition		allRamUsed;

	public static void setController(Controller controller)
	{
		Invoker.controller = controller;
	}

	public static void addObserver(Observer observer)
	{
		Invoker.observers.add(observer);
	}

	public Invoker(long ram) {
		this.maxRam = ram;

		asyncLock = new ReentrantLock();
		allRamUsed = asyncLock.newCondition();
		ramUsed = 0;

		executor = Executors.newFixedThreadPool(MAX_THREADS);

		//TODO: delete this:
		id = ((Integer)numInvokers).toString();
		numInvokers++;
	}

	public long	getAvaiableRam()
	{
		return (maxRam - ramUsed);
	}

	public long	getMaxRam()
	{
		return (maxRam);
	}

	public <T, R> R invoke(Action<Integer, Object> action, T args, String id) throws Exception
	{
		Function<T, R>	function;
		R				result;
		Metric			metric;

		metric = new Metric<T, R>(id, args);
		function = (Function<T, R>) action.getFunction();
		result = function.apply(args);
		notifyAllObservers(metric);
		return (result);
	}

	private <T, R> void notifyAllObservers(Metric<T, R> metric)
	{
		for (Observer observer : observers) {
			observer.update(metric);
		}
		Invoker.controller.addNewMetric(metric);
	}

	// This function tries to execute the function passed by parameter.
	// If there is no space in the pool, it waits and then it gets invoked.
	public <T, R> Future<R> invokeAsync(Action<Integer, Object> action, T args) throws Exception
	{
		Function<T, R>	function;
		
		function = (Function<T, R>) action.getFunction();
		return (executor.submit( () -> {
			R	result;

			asyncLock.lock();
			try {
				System.out.println("Ram used: " + ramUsed + " of " + maxRam + " in invoker " + id);
				while (getAvaiableRam() - action.getRam() < 0)
					allRamUsed.await();
				ramUsed += action.getRam();
				asyncLock.unlock();
				result = function.apply(args);
				} finally {
					asyncLock.lock();
					ramUsed -= action.getRam();
					allRamUsed.signal();
					asyncLock.unlock();
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
