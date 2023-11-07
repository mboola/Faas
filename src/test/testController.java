package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Test;

import application.Controller;
import application.Invoker;
import faas_exceptions.NoInvokerAvaiable;
import faas_exceptions.NoResultAvaiable;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class testController {

	@Test
	public void	controllerCreatedCorrectly()
	{
		int isNUll = 0;
		Controller controller1 = Controller.instantiate();
		if (controller1 == null)
			isNUll = 1;
		assertEquals(isNUll, 0);
		Controller controller2 = Controller.instantiate();
		assertEquals(controller1, controller2);
 	}

	@Test
	public void	controllerRegisterFunctionCorrectly()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);
 	}

	@Test
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

		/* Invoking a function correctly */
		Integer	result = 0;
		Integer	err = 0;
		try {
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
		} catch (NoInvokerAvaiable e1) {
			err = 1;
		} catch (NoResultAvaiable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 1);
		assertEquals(err, 0);

		/* Invoking a function that has not been registered */
		result = 0;
		err = 0;
		try {
			result = (Integer) controller.invoke("hi", Map.of("x", 2, "y", 1));
		} catch (NoInvokerAvaiable e1) {
			err = 1;
		} catch (NoResultAvaiable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 0);
		assertEquals(err, 2);

		/* Invoking a function with an invalid argument */
		result = 0;
		err = 0;
		try {
			result = (Integer) controller.invoke("sub", 2);
		} catch (NoInvokerAvaiable e1) {
			err = 1;
		} catch (NoResultAvaiable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 0);
		assertEquals(err, 3);


		Function<Map<String, Integer>, Integer> f2 = x -> x.get("x") - x.get("y");
		controller.registerAction("sub2", f2, 2);

		/* Invoking a function with not enough RAM */
		result = 0;
		err = 0;
		try {
			result = (Integer) controller.invoke("sub2", Map.of("x", 2, "y", 1));
		} catch (NoInvokerAvaiable e1) {
			err = 1;
		} catch (NoResultAvaiable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		assertEquals(result, 0);
		assertEquals(err, 1);

		//TODO test async function
	}

	@Test
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
		catch (NoInvokerAvaiable e1) {
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
}
