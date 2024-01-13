package testing.composite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import core.application.Action;
import core.application.Controller;
import core.exceptions.*;
import core.invoker.CompositeInvoker;
import core.metrics.MetricCollection;
import observer.InvocationObserver;
import policymanager.GreedyGroup;
import policymanager.RoundRobin;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings("unused")
public class TestGreedyGroupComposite extends InvocationTester {

	private Controller			controller;
	private CompositeInvoker	invokerComposite;

	/*
	 * This gets called before each Test. We create a core controller that
	 * will be used for testing. It will have one InvokerComposite(0) without
	 * childs, one Invoker(1), one IdObserver and one function.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		Action factorial = new FactorialAction();

		invokerComposite = CompositeInvoker.createInvoker(1, 4);
		
		try {
			MetricCollection.instantiate().addObserver(new InvocationObserver());
			controller.registerInvoker(invokerComposite);
			controller.registerAction("Add", f, 1);
			initializeSleepAction("Sleep", 1, controller);
			controller.registerAction("Factorial", factorial, 2);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	/**
	 * Here we will test what happens when nothing is initialized.
	 */
	@Test
	public void	testCompositeNullValues()
	{
		//we don't have a policy manager registered
		assertThrows(NoPolicyManagerRegistered.class, () -> controller.invoke("Add", Map.of("x", 2, "y", 1)));

		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		//here we invoke a function that cannot be invoked from any invoker
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Factorial", 1));
	}

	/**
	 * We will test the behaviour of a controller with one InvokerComposite and one Invoker.
	 */
	@Test
	public void testOneLayerInvokersSync()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(1L), controller);

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e) {
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0 0", str);
	}

	@Test
	public void testOneLayerInvokersAsync()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(1L), controller);

		try {
			List<String> stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 1 1 0 1 0", str);
	}


	/**
	 * We will add two invokers inside(2 & 3) the composite.
	 * Then we will test the RoundRobin policy manager to test all goes as expected.
	 * 
	 * 
	 */
	@Test
	public void	testTwoLayersInvokersSameRamSync()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(1L), controller);
		createAndAddInvokers(Arrays.asList(1L, 1L), invokerComposite);

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e) {
			assertTrue(false);
		}
		
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("2 2 2 2", str);
	}

	@Test
	public void	testTwoLayersInvokersDifferentRamSync()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(2L), controller);
		createAndAddInvokers(Arrays.asList(1L, 2L), invokerComposite);

		try {
			List<Integer> result = controller.invoke("Factorial", 
				Arrays.asList(1L, 1L, 1L, 1L));
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1 1 1 1", str);
	}

	@Test
	public void testTwoLayersInvokersSameRamAsync()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(1L), controller);
		createAndAddInvokers(Arrays.asList(1L, 1L), invokerComposite);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			List<String> stringsResult = invokeList("Sleep", 6, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("2 3 0 1 1 0", str);
	}

	@Test
	public void testTwoLayersInvokersDifferentRamAsync()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}
		
		createAndAddInvokers(Arrays.asList(2L), controller);
		createAndAddInvokers(Arrays.asList(1L, 2L), invokerComposite);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			List<String> stringsResult = invokeList("Sleep", 7, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("1 3 1 2 3 0 1", str);
	}

	//Controller has one CompositeInvoker (ID: 0) and one Invoker (ID: 1) with 2 RAM and
	//This CompositeInvoker (ID: 0) has one Invoker (ID: 2) with 1 ram, another Invoker with 2 ram (ID: 3), and a CompositeInvoker (ID: 4).
	//So 7 invocations should go in the order:
	//the first invoker to fill: (ID: 2) inside 0
	//the second should be (ID: 4)
	//the third and fourth (ID: 3) (ID: 3)
	//the fifth (ID: 0)
	//the sixth and seventh (ID: 1) (ID: 1)
	@Test
	public void testThreeLayersInvokersDifferentRamAsync()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(2L), controller);
		createAndAddInvokers(Arrays.asList(1L, 2L), invokerComposite);
		CompositeInvoker invokerComposite2 = CompositeInvoker.createInvoker(1, 4);

		try {
			invokerComposite.registerInvoker(invokerComposite2);
			long currentTimeMillis = System.currentTimeMillis();
			List<String> stringsResult = invokeList("Sleep", 7, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("2 4 3 3 0 1 1", str);
	}

	@Test
	public void testThreeLayersInvokersDifferentRamAsyncB()
	{
		try {
			controller.setPolicyManager(new GreedyGroup());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(2L), controller);
		createAndAddInvokers(Arrays.asList(1L, 2L), invokerComposite);
		CompositeInvoker invokerComposite2 = CompositeInvoker.createInvoker(1, 4);
		createAndAddInvokers(Arrays.asList(1L, 2L), invokerComposite2);

		try {
			invokerComposite.registerInvoker(invokerComposite2);
			long currentTimeMillis = System.currentTimeMillis();
			List<String> stringsResult = invokeList("Sleep", 10, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("2 5 6 6 4 3 3 0 1 1", str);
	}

}
