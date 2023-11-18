package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Test;

import application.Controller;
import application.Invoker;
import dynamic_proxy.proxies.Calculator;
import dynamic_proxy.proxies.CalculatorProxy;
import dynamic_proxy.proxies.Timer;
import dynamic_proxy.proxies.TimerProxy;
import faas_exceptions.NoActionRegistered;
import faas_exceptions.NoInvokerAvaiable;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class testDynamicProxy {

	@Test
	public void	testDynamicProxySyncFunction()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(2);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);

		Function<Map<String, Integer>, Integer> f1 = x -> x.get("x") + x.get("y");
		controller.registerAction("suma", f1, 1);
		controller.registerAction("calculator", new Calculator(), 1);

		int result = 0;
		int	err = 0;
		try {
			CalculatorProxy calc = (CalculatorProxy)controller.getAction("calculator");
			Future<Integer> res = calc.suma(Map.of("x", 1, "y", 2));
			result = res.get();
		}
		catch (NoActionRegistered e1) {
			err = 1;
		}
		catch (NoInvokerAvaiable e2) {
			err = 2;
		}
		catch (Exception e) {
			err = 3;
		}
		assertEquals(err, 0);
		assertEquals(result, 3);
	}

	@Test
	public void	testDynamicProxyAsyncFunction()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(2);
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
		controller.registerAction("sleep", sleep, 1);
		controller.registerAction("timer", new Timer(), 1);

		int result = 0;
		int	err = 0;
		try {
			long currentTimeMillis = System.currentTimeMillis();
			TimerProxy timer = (TimerProxy)controller.getAction("timer");
			Future<String> res1 = timer.sleep(4);
			Future<String> res2 = timer.sleep(4);
			String str1 = res1.get();
			String str2 = res2.get();
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
			if (totalTime > 4500 || totalTime < 4000)
				result = 0;
			else
				result = 1;
		}
		catch (NoActionRegistered e1) {
			err = 1;
		}
		catch (Exception e) {
			err = 2;
		}
		assertEquals(err, 0);
		assertEquals(result, 1);
	}
}
