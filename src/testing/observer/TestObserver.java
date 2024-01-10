package testing.observer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
		Invoker invoker = Invoker.createInvoker(1);

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
	}

}
