package testing.policy_manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import action.Action;
import action.FactorialAction;
import application.Controller;
import faas_exceptions.NoInvokerAvailable;
import faas_exceptions.OperationNotValid;
import invoker.Invoker;
import observer.IdObserver;
import policy_manager.RoundRobin;

public class TestRoundRobin {

	private Controller controller;

	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		Invoker.setController(controller);

		Function<Integer, String> sleep = s -> {
			long time = Duration.ofMillis(s).toMillis();
			try {
				Thread.sleep(time);
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");
		Invoker.addObserver(new IdObserver());

		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("Sleep", sleep, 1);
			controller.registerAction("Add", f, 1);
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
	}

	private void createAndAddInvokers(List<Long> ramInvokers)
	{
		Invoker invoker;
		for (Long ram : ramInvokers) {
			invoker = Invoker.createInvoker(ram);
			try {
				controller.registerInvoker(invoker);
			}
			catch (Exception e) {
			}
		}
	}

	@Test
	public void testRoundRobinExceptions()
	{
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", 1));
		createAndAddInvokers(Arrays.asList(1L));
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
	public void testRoundRobinOneInvokerSyncFunc()
	{
		createAndAddInvokers(Arrays.asList(1L));
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Add");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinOneInvokerAsyncFunc()
	{
		createAndAddInvokers(Arrays.asList(1L));
		try {
			long currentTimeMillis = System.currentTimeMillis();
			List<Future<String>> futures = new LinkedList<Future<String>>();
			futures = controller.invoke_async("Sleep", Arrays.asList(2000, 2000, 2000));
			List<String> stringsResult = new LinkedList<String>();

			for (Future<String> future : futures)
				stringsResult.add(future.get());
			
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime < 6200 && totalTime > 5800);
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Sleep");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamSyncFunc()
	{
		createAndAddInvokers(Arrays.asList(1L, 1L));
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamAsyncFunc()
	{
		createAndAddInvokers(Arrays.asList(1L, 1L));
		try {
			long currentTimeMillis = System.currentTimeMillis();
			List<Future<String>> futures = new LinkedList<Future<String>>();
			futures = controller.invoke_async("Sleep", Arrays.asList(1500, 1600, 2500, 2400, 3000, 3000));
			List<String> stringsResult = new LinkedList<String>();

			for (Future<String> future : futures)
				stringsResult.add(future.get());
			
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			assertTrue(totalTime < 7200 && totalTime > 6800);
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Sleep");
		assertEquals("101010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamSyncFunc()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L));
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamAsyncFunc()
	{
		Future<String> future1, future2, future3, future4, future5, future6;
		List<String> stringsResult = new LinkedList<String>();

		createAndAddInvokers(Arrays.asList(2L, 1L));

		try {
			future1 = controller.invoke_async("Sleep", 10000);
			Thread.sleep(1000);
			future2 = controller.invoke_async("Sleep",10000);
			Thread.sleep(1000);
			future3 = controller.invoke_async("Sleep", 10000);
			Thread.sleep(1000);
			future4 = controller.invoke_async("Sleep", 10000);
			Thread.sleep(1000);
			future5 = controller.invoke_async("Sleep", 10000);
			Thread.sleep(1000);
			future6 = controller.invoke_async("Sleep", 10000);
			Thread.sleep(1000);
			stringsResult.add(future1.get());
			stringsResult.add(future2.get());
			stringsResult.add(future3.get());
			stringsResult.add(future4.get());
			stringsResult.add(future5.get());
			stringsResult.add(future6.get());
		}
		catch (Exception e) {
		}

		String str = controller.metrics.getData("IdObserver", "Sleep");
		assertEquals("100101", str);
	}
}
