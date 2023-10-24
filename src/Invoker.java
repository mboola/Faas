import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Invoker {
	private static final int MAX_THREADS = 8;
	private int ram;
	private ExecutorService executor;

	public Invoker(int ram) {
		this.ram = ram;
		executor = Executors.newFixedThreadPool(MAX_THREADS);
	}

	public <T, R> R invoke(Function<T, R> action, T args) throws Exception
	{
		return (action.apply(args));
	}

	/**
	 * This function tries to execute the function passed by parameter.
	 * If there is no space in the pool, it waits and then it gets invoked.
	 * @param <T>
	 * @param <R>
	 * @param action
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public <T, R> Future<R> invokeAsync(Function<T, R> action, T args) throws Exception
	{
		return executor.submit( 
			() -> {
				return (action.apply(args));
			}
		);
	}

	public void shutdownInvoker()
	{
		executor.shutdown();
	}
}
