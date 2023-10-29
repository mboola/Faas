import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class Invoker {
	private static final int MAX_THREADS = 8;
	private long			maxRam;
	private long			ramUsed;
	private ExecutorService executor;

	private final Lock			asyncLock;
	private final Condition	allRamUsed;

	public Invoker(long ram) {
		this.maxRam = ram;

		asyncLock = new ReentrantLock();
		allRamUsed = asyncLock.newCondition();
		ramUsed = 0;

		executor = Executors.newFixedThreadPool(MAX_THREADS);
	}

	public long	getAvaiableRam()
	{
		return (maxRam - ramUsed);
	}

	public long	getMaxRam()
	{
		return (maxRam);
	}

	public <T, R> R invoke(Action<Integer, Object> action, T args) throws Exception
	{
		Function<T, R>	function;

		//if ram - ramToUse < 0 wait til its avaiable
		function = (Function<T, R>) action.getFunction();
		return (function.apply(args));
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
				System.out.println("Ram used: " + ramUsed + " of " + maxRam);
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
