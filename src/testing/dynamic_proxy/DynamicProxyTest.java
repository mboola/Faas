package testing.dynamic_proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Test;

import application.Controller;
import application.Invokable;
import decorator.CacheDecorator;
import dynamic_proxy.ActionProxy;
import dynamic_proxy.proxies.Calculator;
import dynamic_proxy.proxies.CalculatorProxy;
import dynamic_proxy.proxies.Timer;
import dynamic_proxy.proxies.TimerProxy;
import faas_exceptions.NoActionRegistered;
import faas_exceptions.NoInvokerAvailable;
import invoker.Invoker;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class DynamicProxyTest {

	@Test
	public void	testDynamicProxySyncFunction()
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(2);
		ActionProxy	actionProxy = new ActionProxy();

		Function<Object, Object> calculator = 
			(obj) -> {
				return (new Calculator());
			}
		;

		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("calculator", calculator, 1);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		int result = 0;
		int	err = 0;
		try {
			CalculatorProxy calc = (CalculatorProxy)actionProxy.getActionProxy("calculator");
			Integer res = calc.suma(Map.of("x", 1, "y", 2));
			assertEquals(3, res);
		}
		catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void	testDynamicProxyAsyncFunction()
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(2);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.setPolicyManager(policyManager);

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
