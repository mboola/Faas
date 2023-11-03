package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import application.Controller;
import application.Invoker;
import application.NoInvokerAvaiable;
import application.NoResultAvaiable;
import application.PolicyManager;
import application.RoundRobin;

public class testController {

	@Test
	public void	controllerCreatedCorrectly()
	{
		Controller controller1 = Controller.instantiate();
		if (controller1 == null) //if it returns null then error
			assertEquals(1, 2);	//TODO: change this way of showing the tests
		else
			assertEquals(1, 1);
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
	}
}
