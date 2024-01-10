package testing.observer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import core.application.Controller;
import core.exceptions.OperationNotValid;
import core.invoker.Invoker;
import core.metrics.MetricSet;
import observer.MemoryObserver;
import observer.TimerObserver;
import policymanager.RoundRobin;

public class TestObserver {

	private Controller controller;

	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(10);

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");

		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("Add", f, 1);
			controller.registerInvoker(invoker);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testObserverExceptions()
	{
		assertThrows(OperationNotValid.class, () -> MetricSet.instantiate().removeObserver(new TimerObserver()));
		assertThrows(OperationNotValid.class, () -> MetricSet.instantiate().addObserver(null));
		try {
			MetricSet.instantiate().addObserver(new TimerObserver());
		}
		catch (OperationNotValid e) {
			assertTrue(false);
		}
		assertThrows(OperationNotValid.class, () -> MetricSet.instantiate().addObserver(new TimerObserver()));
	}

	@Test
	public void testOneObserver()
	{
		try {
			MetricSet.instantiate().addObserver(new TimerObserver());
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricSet.instantiate().getData("TimerObserver", "Add");
		assertTrue(str != null);
	}

	@Test
	public void testTwoObserver()
	{
		String[] strSplit;

		try {
			MetricSet.instantiate().addObserver(new TimerObserver());
			MetricSet.instantiate().addObserver(new MemoryObserver());
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricSet.instantiate().getData("TimerObserver", "Add");
		if (str != null)
		{
			strSplit = str.split(" ");
			assertEquals(strSplit.length, 3);
		}
		else assertTrue(false);

		str = MetricSet.instantiate().getData("MemoryObserver", "Add");
		if (str != null)
		{
			assertEquals("1 1 1", str);
		}
		else assertTrue(false);
	}

	@Test
	public void testTimeMetrics()
	{
		TimerObserver timerObserver = new TimerObserver();

		try {
			MetricSet.instantiate().addObserver(timerObserver);
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("TimerObserver", "Add");
		System.out.println(str);
		Long max = timerObserver.calculateMaxTime("Add");
		System.out.println(max);
		Long min = timerObserver.calculateMinTime("Add");
		System.out.println(min);
		Long average = timerObserver.calculateAverageTime("Add");
		System.out.println(average);
		Long totalTime = timerObserver.calculateAllTime("Add");
		System.out.println(totalTime);
	}

	@Test
	public void testTimeAsyncMetrics()
	{
		TimerObserver timerObserver = new TimerObserver();
		Function<Integer, String> sleep = s -> {
			long time = Duration.ofMillis(s).toMillis();
			try {
				Thread.sleep(time);
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
		try {
			controller.registerAction("Sleep", sleep, 1);
			MetricSet.instantiate().addObserver(timerObserver);
			List<Future<String>> futures = new LinkedList<Future<String>>();
			List<Integer> args = new LinkedList<Integer>();
			args.add(1000);
			args.add(2000);
			args.add(3000);
			args.add(2000);
			args.add(1000);
			args.add(2000);
			List<String> result = new LinkedList<String>();

			futures = controller.invoke_async("Sleep", args);
			for (Future<String> future : futures) {
				result.add(future.get());
			}

		}
		catch (Exception e){
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("TimerObserver", "Sleep");
		System.out.println(str);
		Long max = timerObserver.calculateMaxTime("Sleep");
		System.out.println(max);
		Long min = timerObserver.calculateMinTime("Sleep");
		System.out.println(min);
		Long average = timerObserver.calculateAverageTime("Sleep");
		System.out.println(average);
		Long totalTime = timerObserver.calculateAllTime("Sleep");
		System.out.println(totalTime);
	}

}
