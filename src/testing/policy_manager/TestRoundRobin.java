package testing.policy_manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import action.Action;
import action.FactorialAction;
import application.Controller;
import faas_exceptions.NoInvokerAvailable;
import faas_exceptions.OperationNotValid;
import invoker.Invoker;
import observer.InvocationObserver;
import policy_manager.RoundRobin;
import testing.InvocationTester;

public class TestRoundRobin extends InvocationTester {

	private Controller controller;

	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		Invoker.setController(controller);

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");
		Invoker.addObserver(new InvocationObserver());

		try {
			controller.setPolicyManager(new RoundRobin());
			initializeSleepAction("Sleep", 1, controller);
			controller.registerAction("Add", f, 1);
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
	}

	@Test
	public void testRoundRobinExceptions()
	{
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", 1));
		createAndAddInvokers(Arrays.asList(1L), controller);
		Action factorial = new FactorialAction();
		try {
			controller.registerAction("Factorial", factorial, 2);
		}
		catch (Exception e) {
			assertTrue(false);
		}
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Factorial", 1));
	}

	@Test
	public void testRoundRobinOneInvokerSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("InvocationObserver", "Add");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinOneInvokerSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(2L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("InvocationObserver", "Add");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinOneInvokerAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L), controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 3, 3000, true, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 9200 || totalTime < 8800) assertTrue(false);
			else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = controller.metrics.getData("InvocationObserver", "Sleep");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinOneInvokerAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L), controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 3, 3000, true, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 6200 || totalTime < 5800) assertTrue(false);
			else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = controller.metrics.getData("InvocationObserver", "Sleep");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L, 1L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("InvocationObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(2L, 2L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("InvocationObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, true, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = controller.metrics.getData("InvocationObserver", "Sleep");
		assertEquals("101010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, true, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = controller.metrics.getData("InvocationObserver", "Sleep");
		assertEquals("101010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamSyncFunc()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("InvocationObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, true, controller);
		}
		catch (Exception e) {
		}

		String str = controller.metrics.getData("InvocationObserver", "Sleep");
		assertEquals("100101", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, true, controller);
		}
		catch (Exception e) {
		}

		String str = controller.metrics.getData("InvocationObserver", "Sleep");
		assertEquals("101010", str);
	}

	@Test
	public void testRoundRobinThreeInvokerSameRamSyncFunc()
	{
		createAndAddInvokers(Arrays.asList(1L, 1L, 1L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("InvocationObserver", "Add");
		assertEquals("1201", str);
	}

	
	
}
