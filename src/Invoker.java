import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Invoker {
	private int ram;
	private ExecutorService executor;

	public Invoker(int ram) {
		this.ram = ram;
	}

	public <T, R> R invoke(Function<T, R> action, T args) throws Exception
	{
		return (action.apply(args));
	}

	private <T, R> Future<R> submitExecutor(Function<T, R> action, T args) throws Exception
	{
		return executor.submit( () -> {
			R result = action.apply(args);
			return (result);
		});
	}

	public <T, R> Future<R> invokeAsync(Function<T, R> action, T args) throws Exception
	{
		Future<R> future;

		executor = Executors.newFixedThreadPool(1);
		future = submitExecutor(action, args);
		executor.shutdown();
		return (future);
	}
}
