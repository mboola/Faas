package testing.decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import action.Action;
import action.FactorialAction;
import application.Controller;
import application.Invokable;
import decorator.CacheDecorator;
import faas_exceptions.OperationNotValid;
import invoker.Invoker;
import policy_manager.RoundRobin;

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
		Invoker.setController(controller);
		Invoker invokerSimple = Invoker.createInvoker(1);
		try {
			controller.registerInvoker(invokerSimple);
			controller.setPolicyManager(new RoundRobin());
		} catch (OperationNotValid e) {
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
			
			long cacheResult = Invoker.getCacheResult("Factorial", (long) 20);

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
