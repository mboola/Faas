import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Invoker {
	private static final int MAX_THREADS = 8;
	private long	maxRam;
	private long	ramUsed;
	private ExecutorService executor;

	public Invoker(long ram) {
		this.maxRam = ram;
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

		//if ram - ramToUse < 0 wait til its avaiable
		function = (Function<T, R>) action.getFunction();
		return executor.submit( 
			() -> {
				return (function.apply(args));
			}
		);
	}

	public void shutdownInvoker()
	{
		executor.shutdown();
	}
}
