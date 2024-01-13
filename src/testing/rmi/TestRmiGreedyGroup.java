package testing.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import core.application.Action;
import core.application.Controller;
import core.application.Invokable;
import core.exceptions.*;
import core.invoker.InvokerInterface;
import core.metrics.MetricCollection;
import observer.InvocationObserver;
import policymanager.GreedyGroup;
import rmi.InvocationSet;
import rmi.SerializedFunction;
import rmi.ServerHandler;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings("unused")
public class TestRmiGreedyGroup extends InvocationTester {

	private Controller controller;

	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();

		SerializedFunction<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");
		SerializedFunction<Long, Long> f2 = (x) -> {
			Action<Long, Long> factorial = new FactorialAction();
			return factorial.apply(x);
		};
		SerializedFunction<Integer, String> sleep = s -> {
			long time = Duration.ofMillis(s).toMillis();
			try {
				Thread.sleep(time);
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};

		try {
			controller.setPolicyManager(new GreedyGroup());
			controller.registerAction("Add", f, 1);
			controller.registerAction("Factorial", f2, 2);
			controller.registerAction("Sleep", sleep, 1);
			MetricCollection.instantiate().addObserver(new InvocationObserver());
		}
		catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testRMIGreedyGroupExceptions()
	{
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", 1));
		createAndAddInvokers(Arrays.asList(1L), controller);
	}

	@Test
	public void testRMIGreedyGroupOneInvokerSyncFuncA()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(1L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupOneInvokerSyncFuncB()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(2L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupTwoInvokerSameRamSyncFuncA()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(1L, 1L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupTwoInvokerSameRamSyncFuncB()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(2L, 2L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupTwoInvokerDifferentRamSyncFuncA()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(1L, 2L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupTwoInvokerDifferentRamSyncFuncB()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(1L, 2L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1 1", str);
	}

	@Test
	public void testRMIGreedyGroupThreeInvokerSameRamSyncFuncA()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(1L, 1L, 1L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), 
				Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0 0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupThreeInvokerSameRamSyncFuncB()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(2L, 2L, 2L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), 
				Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0 0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupThreeInvokerDifferentRamSyncFuncA()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(1L, 2L, 3L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), 
				Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 0 0 0 0", str);
	}

	@Test
	public void testRMIGreedyGroupThreeInvokerDifferentRamSyncFuncB()
	{
		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(1L, 2L, 3L), null, null);

		String serializedControllerInformation = InvocationSet.serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});
		
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L, 1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1 1 1 1", str);
	}
	
}
