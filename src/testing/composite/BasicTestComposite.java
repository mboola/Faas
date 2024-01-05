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
import core.metrics.MetricSet;
import observer.InvocationObserver;
import policymanager.RoundRobin;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings("unused")
public class BasicTestComposite extends InvocationTester {

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

		invokerComposite = CompositeInvoker.createInvoker(1);

		MetricSet.instantiate().addObserver(new InvocationObserver());
		
		try {
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
			controller.setPolicyManager(new RoundRobin());
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
			controller.setPolicyManager(new RoundRobin());
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testOneLayerInvokersAsync()
	{
		try {
			controller.setPolicyManager(new RoundRobin());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(1L), controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			List<String> stringsResult = invokeList("Sleep", 3, 3000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 6200 || totalTime < 5800) assertTrue(false);
			else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("101", str);
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
			controller.setPolicyManager(new RoundRobin());
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
		
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("1312", str);
	}

	@Test
	public void	testTwoLayersInvokersDifferentRamSync()
	{
		try {
			controller.setPolicyManager(new RoundRobin());
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

		String str = MetricSet.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1313", str);
	}

	@Test
	public void testTwoLayersInvokersSameRamAsync()
	{
		try {
			controller.setPolicyManager(new RoundRobin());
		} catch (Exception e) {
			assertTrue(false);
		}

		createAndAddInvokers(Arrays.asList(1L), controller);
		createAndAddInvokers(Arrays.asList(1L, 1L), invokerComposite);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			List<String> stringsResult = invokeList("Sleep", 6, 6000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			//if (totalTime > 6200 || totalTime < 5800) assertTrue(false);
			//else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("132010", str);
	}

	@Test
	public void testTwoLayersInvokersDifferentRamAsync()
	{
		try {
			controller.setPolicyManager(new RoundRobin());
		} catch (Exception e) {
			assertTrue(false);
		}
		
		createAndAddInvokers(Arrays.asList(2L), controller);
		createAndAddInvokers(Arrays.asList(1L, 2L), invokerComposite);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			List<String> stringsResult = invokeList("Sleep", 7, 7000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			//if (totalTime > 6200 || totalTime < 5800) assertTrue(false);
			//else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("1312301", str);
	}

}
