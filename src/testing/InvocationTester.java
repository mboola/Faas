package testing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import core.application.Controller;
import core.invoker.CompositeInvoker;
import core.invoker.Invoker;

public class InvocationTester {

	protected void initializeSleepAction(String name, int ram, Controller controller)
	{
		Function<Integer, String> sleep = s -> {
			long time = Duration.ofMillis(s).toMillis();
			try {
				Thread.sleep(time);
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
		try {
			controller.registerAction(name, sleep, ram);
		}
		catch (Exception e) {
			assertTrue(false);
		}
	}

	protected void createAndAddInvokers(List<Long> ramInvokers, Controller controller)
	{
		Invoker invoker;
		for (Long ram : ramInvokers) {
			invoker = Invoker.createInvoker(ram, 4);
			try {
				controller.registerInvoker(invoker);
			}
			catch (Exception e) {
			}
		}
	}

	protected void createAndAddInvokers(List<Long> ramInvokers, CompositeInvoker composite)
	{
		Invoker invoker;
		for (Long ram : ramInvokers) {
			invoker = Invoker.createInvoker(ram, 4);
			try {
				composite.registerInvoker(invoker);
			}
			catch (Exception e) {
			}
		}
	}

	protected <T, R> List<R> invokeList(String id, int numCalls, T data, Controller controller)
	{
		List<Future<R>> futures = new LinkedList<Future<R>>();
		List<T> args = new LinkedList<T>();
		List<R> result = new LinkedList<R>();
		
		for (int i = 0; i < numCalls; i++) {
			args.add(data);
		}
		try {
			futures = controller.invoke_async(id, args);
		} catch (Exception e) {
			assertTrue(false);
			e.printStackTrace();
		}
		for (Future<R> future : futures) {
			try {
				result.add(future.get());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return (result);
	}

}
