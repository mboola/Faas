package testing.dynamicproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Timer;
import java.util.function.Function;

import org.junit.Test;

import core.application.Controller;
import core.dynamicproxy.DynamicProxy;
import core.invoker.Invoker;
import policymanager.RoundRobin;
import services.proxies.Calculator;
import services.proxies.CalculatorProxy;
import services.proxies.TimerProxy;



public class DynamicProxyTest {

	@Test
	public void	testThrows()
	{

	}

	@Test
	public void testSimpleFunctionality() {
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

			Integer result = calculatorProxy.suma(Map.of("x", 4, "y", 2));
			assertEquals(result, 6);
			controller.shutdownAllInvokers();
		}
		catch (Exception e) {
			assertTrue(false);
		}
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

			Integer result = calculatorProxy.suma(Map.of("x", 4, "y", 2));
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

			//long currentTimeMillis = System.currentTimeMillis();
			//Object result = timerProxy.sleep(2);
			//String finalResult = ((Future<String>)result).get();
			//long totalTime = System.currentTimeMillis() - currentTimeMillis;

			//assertTrue(totalTime < 2100 && totalTime > 1900);
			controller.shutdownAllInvokers();
		}
		catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
