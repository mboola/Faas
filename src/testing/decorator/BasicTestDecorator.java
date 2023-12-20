package testing.decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import action.Action;
import action.FactorialAction;
import application.Controller;
import application.Invokable;
import decorator.Cache;
import decorator.CacheDecorator;
import invoker.Invoker;
import policy_manager.RoundRobin;

@SuppressWarnings({"unchecked", "unused"})
public class BasicTestDecorator {
    
	static final long FACT_20=(long) 2432902008176640000L;
    private Controller	controller;

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
	public void	testCacheDecoratorFactorial()
	{
		/*
		 * Function<Invokable, Function<Object,Object>> decoratorInitializer = 
			(invokable) -> {
				Function<Object, Object>	cacheDecorator;
				String						id;
				Function<Object, Object>	function;

				function = (Function<Object, Object>)invokable.getInvokable();
				id = invokable.getId();
				cacheDecorator = new CacheDecorator<>(function, id);
				return (cacheDecorator);
			}
		;
		 */
		
		Action<Long, Long> f = new FactorialAction();
		try {
			controller.registerAction("Factorial", f, 1);

			long currentTimeMillis = System.currentTimeMillis();
			long result = (long) controller.invoke("Factorial", (long) 20);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertEquals(FACT_20, (long) result);
			
			long cacheResult = Cache.instantiate().getCacheResult("Factorial", (long) 20);

			assertEquals(FACT_20, cacheResult);
		}
		catch (Exception e1) {
			assertTrue(false);
		}
	}

	@Test
	public void	testCacheDecoratorSleep()
	{
		/*
		 * Function<Invokable, Function<Object,Object>> decoratorInitializer = 
			(invokable) -> {
				Function<Object, Object>	cacheDecorator;
				String						id;
				Function<Object, Object>	function;

				function = (Function<Object, Object>)invokable.getInvokable();
				id = invokable.getId();
				cacheDecorator = new CacheDecorator<>(function, id);
				return (cacheDecorator);
			}
		;
		 */

		/*
		 * try {
			controller.registerAction("Sleep", sleep, 1);

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
		 */
	}

}
