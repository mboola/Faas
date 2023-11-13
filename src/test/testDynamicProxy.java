package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import application.Controller;
import application.Invoker;
import dynamic_proxy.ActionProxy;
import dynamic_proxy.Calculator;
import dynamic_proxy.DynamicProxy;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class testDynamicProxy {

	@Test
	public void	dynamicProxyTest()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);

		Function<Map<String, Integer>, Integer> f1 = x -> x.get("x") + x.get("y");
		controller.registerAction("suma", f1, 2);
		controller.registerAction("calculator", new Calculator(), 1);

		int result = 0;
		int	err = 0;
		try {
			Calculator calc = (Calculator)controller.getAction("calculator");
			result = (int)calc.suma(Map.of("x", 1, "y", 2));
		}
		catch (Exception e) {
			err = 1;
		}
		assertEquals(err, 0);
		assertEquals(result, 3);
	}
}
