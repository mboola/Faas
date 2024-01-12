package testing.observer;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import core.metrics.MetricCollection;
import observer.IdObserver;
import observer.MemoryObserver;
import observer.TimerObserver;
import policymanager.RoundRobin;

@SuppressWarnings("unused")
public class TestObserver {

	private Controller controller;

	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		Invoker invoker1 = Invoker.createInvoker(5, 4);
		Invoker invoker2 = Invoker.createInvoker(5, 4);

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");

		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("Add", f, 1);
			controller.registerInvoker(invoker1);
			controller.registerInvoker(invoker2);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testObserverExceptions()
	{
		assertThrows(OperationNotValid.class, () -> MetricCollection.instantiate().removeObserver(new TimerObserver()));
		assertThrows(OperationNotValid.class, () -> MetricCollection.instantiate().addObserver(null));
		try {
			MetricCollection.instantiate().addObserver(new TimerObserver());
		}
		catch (OperationNotValid e) {
			assertTrue(false);
		}
		assertThrows(OperationNotValid.class, () -> MetricCollection.instantiate().addObserver(new TimerObserver()));
	}

	@Test
	public void testOneObserver()
	{
		try {
			MetricCollection.instantiate().addObserver(new TimerObserver());
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("TimerObserver", "Add");
		assertTrue(str != null);
	}

	@Test
	public void testTwoObserver()
	{
		String[] strSplit;

		try {
			MetricCollection.instantiate().addObserver(new TimerObserver());
			MetricCollection.instantiate().addObserver(new MemoryObserver());
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e) {
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("TimerObserver", "Add");
		if (str != null)
		{
			strSplit = str.split(" ");
			assertEquals(strSplit.length, 3);
		}
		else assertTrue(false);

		str = MetricCollection.instantiate().getData("MemoryObserver", "Add");
		if (str != null)
			assertEquals("1 1 1", str);
		else assertTrue(false);
	}

	@Test
	public void testTimeMetrics()
	{
		TimerObserver timerObserver = new TimerObserver();

		try {
			MetricCollection.instantiate().addObserver(timerObserver);
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("TimerObserver", "Add");
		String[] strSplit = str.split(" ");
		assertEquals(strSplit.length, 3);
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
			MetricCollection.instantiate().addObserver(timerObserver);
			List<Future<String>> futures = new LinkedList<Future<String>>();
			List<Integer> args = new LinkedList<Integer>();
			args.add(1000);	//1 sec
			args.add(2000);	//2 sec
			args.add(3000);	//3 sec
			args.add(2000);	//2 sec
			args.add(1000);	//1 sec
			args.add(2000);	//2 sec
			List<String> result = new LinkedList<String>();

			futures = controller.invoke_async("Sleep", args);
			for (Future<String> future : futures) {
				result.add(future.get());
			}
		}
		catch (Exception e){
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("TimerObserver", "Sleep");
		System.out.println(str);
		//max should be more or less 3 sec
		Long max = timerObserver.calculateMaxTime("Sleep");
		assertTrue(max > 2800000000L && max < 3200000000L);
		System.out.println(max);
		//min should be more or less 1 sec
		Long min = timerObserver.calculateMinTime("Sleep");
		assertTrue(min > 800000000L && min < 1200000000L);
		System.out.println(min);
		//totalTime should be more or less 11 sec
		Long totalTime = timerObserver.calculateAllTime("Sleep");
		assertTrue(totalTime > 10800000000L && totalTime < 11200000000L);
		System.out.println(totalTime);
		//average should be more or less 1.8 sec
		Long average = timerObserver.calculateAverageTime("Sleep");
		assertTrue(average > 1600000000L && average < 2000000000L);
		System.out.println(average);
	}

	@Test
	public void testInvocationsAsyncMetrics()
	{
		IdObserver idObserver = new IdObserver();
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
			MetricCollection.instantiate().addObserver(idObserver);
			List<Future<String>> futures = new LinkedList<Future<String>>();
			List<Integer> args = new LinkedList<Integer>();
			args.add(1000);	//1 sec
			args.add(2000);	//2 sec
			args.add(3000);	//3 sec
			args.add(2000);	//2 sec
			args.add(1000);	//1 sec
			args.add(2000);	//2 sec
			List<String> result = new LinkedList<String>();

			futures = controller.invoke_async("Sleep", args);
			for (Future<String> future : futures) {
				result.add(future.get());
			}
		}
		catch (Exception e){
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("IdObserver", "Sleep");
		System.out.println(str);
		Long timesInvoked0Used = idObserver.calculateAllTimesInvoked("Sleep", "0");
		System.out.println(timesInvoked0Used);
		assertEquals(timesInvoked0Used, 3L);
	}

}
