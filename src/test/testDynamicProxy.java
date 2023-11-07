package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import application.Controller;
import application.Invoker;
import dynamic_proxy.ActionProxy;
import dynamic_proxy.DynamicProxy;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class testDynamicProxy {

	@Test
	public void	dynamicProxyTest()
	{
		Controller controller = Controller.instantiate();
		ActionProxy actionProxy = (ActionProxy) DynamicProxy.newInstance(controller);
		Invoker.setController((Controller) controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);

		Function<Map<String, Integer>, Integer> f1 = x -> x.get("x") - x.get("y");
		actionProxy.registerAction("sub", f1, 2);
		/*
		 * int result = 0;
		int	err = 0;
		try {
			result = actionProxy.sub(Map.of("x", 5, "y", 2));
		}
		catch (Exception e) {
			err = 1;
		}
		assertEquals(result, 3);
		assertEquals(err, 0);
		 */
	}

}