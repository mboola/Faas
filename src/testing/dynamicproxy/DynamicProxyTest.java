package testing.dynamicproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Function;
import java.util.concurrent.Future;

import org.junit.Test;

import core.application.Controller;
import core.dynamicproxy.DynamicProxy;
import core.exceptions.NoActionRegistered;
import core.exceptions.NoInvokerAvailable;
import core.exceptions.OperationNotValid;
import core.invoker.Invoker;
import policymanager.RoundRobin;
import services.proxies.Calculator;
import services.proxies.CalculatorProxy;
import services.proxies.Timer;
import services.proxies.TimerProxy;


public class DynamicProxyTest {

	@Test
	public void	testThrows()
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(200);

		Function<Object, Object> calculator = 
			(obj) -> {
				return (new Calculator());
			}
		;

		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
		}
		catch (Exception e) {
			assertTrue(false);
		}

		//Exception getting a proxy that has not been registered
		assertThrows(NoActionRegistered.class, () -> DynamicProxy.getActionProxy("CalculatorService", true));

		//Exception getting a null id proxy
		assertThrows(OperationNotValid.class, () -> DynamicProxy.getActionProxy(null, true));

		try {
			controller.registerAction("CalculatorService", calculator, 1);
		} catch (Exception e) {
			assertTrue(false);
		}
	
		//not sure about this test but whatever
		assertThrows(RuntimeException.class, () -> {
			CalculatorProxy calculatorProxy = (CalculatorProxy)DynamicProxy.getActionProxy("CalculatorService", true);
			calculatorProxy.suma(null);
		});
	}

	@Test
	public void	testDynamicProxySyncFunction()
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(200);

		Function<Object, Object> calculator = 
			(obj) -> {
				return (new Calculator());
			}
		;

		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("CalculatorService", calculator, 1);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		try {
			CalculatorProxy calculatorProxy = (CalculatorProxy)DynamicProxy.getActionProxy("CalculatorService", true);

			Integer result = (Integer)calculatorProxy.suma(Map.of("x", 4, "y", 2));
			assertEquals(result, 6);
			controller.shutdownAllInvokers();
		}
		catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void	testDynamicProxyAsyncFunction()
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(200);

		Function<Object, Object> timer = 
			(obj) -> {
				return (new Timer());
			}
		;

		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("TimerService", timer, 1);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		try {
			TimerProxy timerProxy = (TimerProxy)DynamicProxy.getActionProxy("TimerService", false);

			long currentTimeMillis = System.currentTimeMillis();

			Object result = timerProxy.waitSec(2000);
			
			String finalResult = ((Future<String>)result).get();
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime < 2100 && totalTime > 1900);
			controller.shutdownAllInvokers();
		}
		catch (Exception e) {
			assertTrue(false);
		}
	}
}
