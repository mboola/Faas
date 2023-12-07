package test.composite;

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

import application.Controller;
import faas_exceptions.NoInvokerAvailable;
import faas_exceptions.NoPolicyManagerRegistered;
import faas_exceptions.OperationNotValid;
import invoker.Invoker;
import invoker.InvokerComposite;
import observer.IdObserver;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class BasicTestComposite {

	private Controller			controller;
	private InvokerComposite	invokerComposite;

	/*
	 * This gets called before each Test. We create a core controller that
	 * will be used for testing. It will have one InvokerComposite(0) without
	 * childs, one Invoker(1), one IdObserver and one function.
	 */
	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		Invoker.setController(controller);
		invokerComposite = InvokerComposite.createInvoker(1);
		Invoker invokerSimple = Invoker.createInvoker(2);
		Invoker.addObserver(new IdObserver());
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		try {
			controller.registerInvoker(invokerComposite);
			controller.registerInvoker(invokerSimple);
			controller.registerAction("Substract", f, 1);
		} catch (OperationNotValid e) {
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
		assertThrows(NoPolicyManagerRegistered.class, () -> controller.invoke("Substract", Map.of("x", 2, "y", 1)));

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");
		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("Add", f, 3);
		} catch (OperationNotValid e) {
			assertTrue(false);
		}

		//here we invoke a function that cannot be invoked from any invoker
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", Map.of("x", 2, "y", 1)));
	}

	/**
	 * We will test the behaviour of a controller with one InvokerComposite and one Invoker.
	 */
	@Test
	public void testOneLayerInvokersSync()
	{
		// Here we will do multiple invokations
		try {
			controller.setPolicyManager(new RoundRobin());

			Integer result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
		}
		catch (NoInvokerAvailable e) {
			assertTrue(false);
		}
		catch (OperationNotValid e) {
			assertTrue(false);
		}
		catch (Exception e) {
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Substract");
		assertEquals(str, "1010");
	}

	@Test
	public void testOneLayerInvokersAsync()
	{
		Function<Integer, String> sleep = s -> {
			try {
				Thread.sleep(Duration.ofSeconds(s).toMillis());
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};

		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("Sleep", sleep, 2);

			long currentTimeMillis = System.currentTimeMillis();
			List<Future<String>> futures = new LinkedList<Future<String>>();
			futures = controller.invoke_async("Sleep", Arrays.asList(2,2));
			List<String> stringsResult = new LinkedList<String>();

			for (Future<String> future : futures)
				stringsResult.add(future.get());

			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 4500 || totalTime < 4000) assertTrue(false);
			else assertTrue(true);
		} 
		catch (Exception e) {
			assertTrue(false);
		}
	}


	/**
	 * We will add two invokers inside(2 & 3) the composite.
	 * Then we will test the RoundRobin policy manager to test all goes as expected.
	 * 
	 * 
	 */
	@Test
	public void	testTwoLayersInvokersSyncSameRam()
	{
		try {
			controller.setPolicyManager(new RoundRobin());
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
		Invoker invokerSimple1 = Invoker.createInvoker(1);
		Invoker invokerSimple2 = Invoker.createInvoker(1);
		try
		{
			invokerComposite.registerInvoker(invokerSimple1);
			invokerComposite.registerInvoker(invokerSimple2);
			Integer result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Substract");
		assertEquals(str, "1312");
	}

	@Test
	public void	testTwoLayersInvokersSyncDifferentRam()
	{
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");

		Invoker invokerSimple1 = Invoker.createInvoker(1);
		Invoker invokerSimple2 = Invoker.createInvoker(2);

		try
		{
			controller.registerAction("Add", f, 2);
			controller.setPolicyManager(new RoundRobin());

			invokerComposite.registerInvoker(invokerSimple1);
			invokerComposite.registerInvoker(invokerSimple2);
			Integer result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
			result = (Integer) controller.invoke("Substract", Map.of("x", 2, "y", 1));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = controller.metrics.getData("IdObserver", "Substract");
		assertEquals(str, "1212");
	}

}
