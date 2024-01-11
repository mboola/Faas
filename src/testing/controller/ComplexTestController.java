package testing.controller;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import core.application.Action;
import core.application.Controller;
import core.exceptions.NoActionRegistered;
import core.exceptions.NoInvokerAvailable;
import core.exceptions.OperationNotValid;
import core.invoker.Invoker;
import policymanager.RoundRobin;
import services.otheractions.AddAction;


/**
 * The BasicTestController class contains more complex test cases for the Controller class.
 * Checks correct registration and invokation of actions and functions.
 * Also checks correct registration of classes.
 */
public class ComplexTestController {
    
	private Controller controller;

	/*
	 * This gets called before each Test. 
	 * We instantiate a controller with basic components so it can work.
	 * (for some reason BeforeEach couldn't be used)
	 */
	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(1);
		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void	testCheckThrows()
	{
		long ram = 1;
		assertThrows(OperationNotValid.class, () -> controller.registerAction("id", null, ram));
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		assertThrows(OperationNotValid.class, () -> controller.registerAction(null, f, ram));
		try {
			controller.registerAction("test", f, ram);
			assertThrows(OperationNotValid.class, () -> controller.registerAction("test", f, ram));
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
	}

	@Test
	public void testRegisterAction()
	{
		long ram = 1;
		Action<Map<String, Integer>, Integer> action = new AddAction();
		try {
			controller.registerAction("addAction", action, ram);
			assertSame(action, controller.getAction("addAction"));
		} catch (OperationNotValid e) {
			assertTrue(false);
		}

		Integer result = 0;
		try {
			result = (Integer) controller.invoke("addAction", Map.of("x", 2, "y", 1));
		} catch (Exception e1) {
			assertTrue(false);
		}
		assertEquals(result, 3);
	}

	@Test
	public void testRegisterAndInvokeFunction()
	{
		long ram = 1;
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		try {
			controller.registerAction("sub", f, ram);
			assertSame(f, controller.getAction("sub"));
		} catch (OperationNotValid e) {
			assertTrue(false);
		}

		Integer result = 0;
		try {
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
		} catch (Exception e1) {
			assertTrue(false);
		}
		assertEquals(result, 1);
	}
	
    @Test
	public void	functionInvokedCorrectly()
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(1);

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");

		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("sub", f, 1);
		} catch (Exception e) {
			assertTrue(false);
		}

		// Invoking a function correctly 
		Integer	result = 0;
		try {
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
		} catch (Exception e) {
			assertTrue(false);
		}
		assertEquals(result, 1);

		//Invoking a function that has not been registered
		assertThrows(NoActionRegistered.class, () -> controller.invoke("hi", Map.of("x", 2, "y", 1)));

		// Invoking a function with an invalid argument
		assertThrows(Exception.class, () -> controller.invoke("sub", 2));

		// Passing a null as the id of the function to execute
		assertThrows(OperationNotValid.class, () -> controller.invoke(null, 1));

		Function<Map<String, Integer>, Integer> f2 = x -> x.get("x") - x.get("y");
		try {
			controller.registerAction("sub2", f2, 2);
		} catch (Exception e) {
			assertTrue(false);
		}

		// Invoking a function with not enough RAM
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("sub2", Map.of("x", 2, "y", 1)));
	}
	
	@Test
	public void	asyncFunctionTests()
	{
		Function<Integer, String> sleep = s -> {
			try {
				Thread.sleep(Duration.ofSeconds(s).toMillis());
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};

		try {
			controller.registerAction("sleepAction", sleep, 1);
		} catch (Exception e) {
			assertTrue(false);
		}

		try {
			long currentTimeMillis = System.currentTimeMillis();
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 2; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
			if (totalTime > 4500 || totalTime < 4000)
				assertTrue(false);
			else
				assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}

	}

}
