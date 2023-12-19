package testing.decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
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

	@Test
	public void	testCacheDecoratorOverlapp()
	{
		Function<Invokable, Function<Object,Object>> decoratorInitializer = 
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
		Invoker.setDecoratorInitializer(decoratorInitializer);
		Function<List<Integer>, Integer> f = x -> x.get(0) + x.get(1);
		var f2 = new CacheDecorator<>(f, null);

		try {
			controller.registerAction("Addition", f2, 1);

			List<Integer> args1 = new LinkedList<Integer>();
			args1.add(11);
			args1.add(0);
			List<Integer> args2 = new LinkedList<Integer>();
			args2.add(1);
			args2.add(10);
			List<List<Integer>> singleList = new LinkedList<List<Integer>>();
			singleList.add(args1);
			singleList.add(args2);
			
			long currentTimeMillis = System.currentTimeMillis();
			List<Integer> result = controller.invoke("Addition", singleList);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			Cache.instantiate().printCache();
		}
		catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void	testCacheDecoratorFactorial()
	{
		Function<Invokable, Function<Object,Object>> decoratorInitializer = 
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
		Invoker.setDecoratorInitializer(decoratorInitializer);
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
		Function<Invokable, Function<Object,Object>> decoratorInitializer = 
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
		Invoker.setDecoratorInitializer(decoratorInitializer);

		Function<Integer, String> sleep = s -> {
			try {
				Thread.sleep(Duration.ofSeconds(s).toMillis());
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};

		try {
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
	}

}
