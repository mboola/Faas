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
import decorator.TimerDecorator;
import policymanager.RoundRobin;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings({"unchecked", "unused", "rawtypes"})
public class BasicTestDecorator extends InvocationTester {
    
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
	public void	testCacheDecoratorHashmapOverlapp()
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
	public void	testCacheDecoratorAsync()
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
			stringsResult = invokeList("Sleep", 1, 2000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime <= 2100);

			currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 1, 2000, controller);
			totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime <= 0);

			Cache.instantiate().printCache();
		}
		catch (Exception e1) {
			assertTrue(false);
		}
	}

	@Test
	public void	testTimerDecoratorFactorial()
	{
		Action factorial = new FactorialAction();
		TimerDecorator timerDecorator = new TimerDecorator<>(factorial); 

		try {
			controller.registerAction("factorial", timerDecorator, 1);

			Long[] longArray = {5L, 2L, 3L, 5L, 4L, 1L};
			List<Long>	input = Arrays.asList(longArray);
			
			List<Integer> result = controller.invoke("factorial", input);
		}
		catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void	testTimerDecoratorAsync()
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
		TimerDecorator timerDecorator = new TimerDecorator<>(sleep); 
		List<String> stringsResult;

		try {
			controller.registerAction("Sleep", timerDecorator, 1);

			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 4, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime <= 4200);

			Cache.instantiate().printCache();
		}
		catch (Exception e1) {
			assertTrue(false);
		}
	}

	@Test
	public void	testTimerCacheDecoratorFactorial()
	{
		Action factorial = new FactorialAction();
		TimerDecorator timerDecorator = new TimerDecorator<>(factorial); 
		CacheDecorator cacheDecorator = new CacheDecorator<>(timerDecorator, "factorial");

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
	public void	testCacheTimerDecoratorFactorial()
	{
		Action factorial = new FactorialAction();
		CacheDecorator cacheDecorator = new CacheDecorator<>(factorial, "factorial");
		TimerDecorator timerDecorator = new TimerDecorator<>(cacheDecorator); 

		try {
			controller.registerAction("factorial", timerDecorator, 1);

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
	public void	testTimerCacheDecoratorAsync()
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
		TimerDecorator timerDecorator = new TimerDecorator<>(sleep); 
		CacheDecorator cacheDecorator = new CacheDecorator<>(timerDecorator, "Sleep");
		List<String> stringsResult;

		try {
			controller.registerAction("Sleep", cacheDecorator, 1);

			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 4, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime <= 4200);

			Cache.instantiate().printCache();
		}
		catch (Exception e1) {
			assertTrue(false);
		}
	}

	@Test
	public void	testCacheTimerDecoratorAsync()
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
		TimerDecorator timerDecorator = new TimerDecorator<>(cacheDecorator); 
		List<String> stringsResult;

		try {
			controller.registerAction("Sleep", cacheDecorator, 1);

			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 4, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime <= 4200);

			Cache.instantiate().printCache();
		}
		catch (Exception e1) {
			assertTrue(false);
		}
	}

}
