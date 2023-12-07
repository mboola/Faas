package testing.controller;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import action.Action;
import action.AddAction;
import application.Controller;
import dynamic_proxy.proxies.Calculator;
import faas_exceptions.OperationNotValid;
import invoker.Invoker;
import policy_manager.RoundRobin;

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
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
		System.out.println("Controller instantiated");
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
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
		assertSame(action, controller.getAction("addAction"));

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
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
		assertSame(f, controller.getAction("sub"));

		Integer result = 0;
		try {
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
		} catch (Exception e1) {
			assertTrue(false);
		}
		assertEquals(result, 1);
	}

	@Test
	public void	testRegisterClass()
	{
		long ram = 1;
		Calculator calculator = new Calculator();
		try {
			controller.registerAction("calculator", calculator, ram);
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
		assertSame(calculator, controller.getAction("calculator"));
	}
	
    /*
	 * @Test
	public void	functionInvokedCorrectly()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		controller.registerAction("sub", f, 1);

		// Invoking a function correctly 
		Integer	result = 0;
		Integer	err = 0;
		try {
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (NoResultAvailable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 1);
		assertEquals(err, 0);

		//Invoking a function that has not been registered
		result = 0;
		err = 0;
		try {
			result = (Integer) controller.invoke("hi", Map.of("x", 2, "y", 1));
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (NoActionRegistered e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 0);
		assertEquals(err, 2);

		// Invoking a function with an invalid argument
		result = 0;
		err = 0;
		try {
			result = (Integer) controller.invoke("sub", 2);
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (NoResultAvailable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 0);
		assertEquals(err, 3);


		Function<Map<String, Integer>, Integer> f2 = x -> x.get("x") - x.get("y");
		controller.registerAction("sub2", f2, 2);

		// Invoking a function with not enough RAM
		result = 0;
		err = 0;
		try {
			result = (Integer) controller.invoke("sub2", Map.of("x", 2, "y", 1));
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (NoResultAvailable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 0);
		assertEquals(err, 1);

		//TODO test async function
	}
	 */

	/*
	 * @Test
	public void	asyncFunctionTests()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);

		Function<Integer, String> sleep = s -> {
			try {
				Thread.sleep(Duration.ofSeconds(s).toMillis());
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
		controller.registerAction("sleepAction", sleep, 1);

		long currentTimeMillis = System.currentTimeMillis();
		try {
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
		}
		catch (NoInvokerAvailable e1) {
			System.out.println(e1.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long finalimeMillis = System.currentTimeMillis();
		long totalTime = finalimeMillis - currentTimeMillis;

		System.out.println(" Seconds:" + totalTime);
		int result;
		if (totalTime > 4500 || totalTime < 4000)
			result = 0;
		else
			result = 1;
		//IMPORTANT: do not use cache decorator or this test will fail
		assertEquals(result, 1);
	}
	 */

}
