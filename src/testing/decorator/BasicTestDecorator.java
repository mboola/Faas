package testing.decorator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import core.application.Action;
import core.application.Controller;
import core.invoker.Invoker;
import decorator.Cache;
import decorator.CacheDecorator;
import policymanager.RoundRobin;
import services.otheractions.FactorialAction;

@SuppressWarnings({"unchecked", "unused", "rawtypes"})
public class BasicTestDecorator {
    
	static final long FACT_20=(long) 2432902008176640000L;
    private Controller controller;

	/*
	 * This gets called before each Test. We create a core controller that
	 * will be used for testing. It will have one InvokerComposite(0) without
	 * childs, one Invoker(1), one IdObserver and one function.
	 */
	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		Invoker invokerSimple = Invoker.createInvoker(1);
		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerInvoker(invokerSimple);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	/*
	 * This tests checks if creating
	 */
	@Test
	public void	testCacheDecoratorHashmapOverlapping()
	{
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");
		CacheDecorator cacheDecorator = new CacheDecorator<>(f, "add");

		try {
			controller.registerAction("add", cacheDecorator, 1);
			
			Integer result1 = controller.invoke("add", Map.of("x", 11, "y", 0));
			Integer result2 = controller.invoke("add", Map.of("x", 1, "y", 10));
			Integer result3 = controller.invoke("add", Map.of("x", 1, "y", 10));

			Cache.instantiate().printCache();
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void	testCacheDecoratorFactorial()
	{
		Action factorial = new FactorialAction();
		CacheDecorator cacheDecorator = new CacheDecorator<>(factorial, "factorial");

		try {
			controller.registerAction("factorial", cacheDecorator, 1);

			Long[] longArray = {1L, 2L, 3L, 4L, 5L, 1L};
			List<Long>	input = Arrays.asList(longArray);
			
			List<Integer> result = controller.invoke("factorial", input);

			Cache.instantiate().printCache();
		}
		catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void	testCacheDecoratorSleep()
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
		CacheDecorator cacheDecorator = new CacheDecorator<>(sleep, "Sleep");
		List<String> stringsResult;

		try {
			controller.registerAction("Sleep", cacheDecorator, 1);

			long currentTimeMillis = System.currentTimeMillis();
			String result = (String) controller.invoke("Sleep", 2);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime <= 2100);

			currentTimeMillis = System.currentTimeMillis();
			result = (String) controller.invoke("Sleep", 2);
			totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime <= 0);
		}
		catch (Exception e1) {
			assertTrue(false);
		}
	}

}
