package testing.policymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import core.application.Action;
import core.application.Controller;
import core.exceptions.*;
import core.metrics.MetricCollection;
import observer.InvocationObserver;
import policymanager.BigGroup;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings("unused")
public class TestBigGroup extends InvocationTester {

	private Controller controller;

	@Before
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");
		Action factorial = new FactorialAction();

		try {
			MetricCollection.instantiate().addObserver(new InvocationObserver());
			controller.setPolicyManager(new BigGroup());
			initializeSleepAction("Sleep", 1, controller);
			controller.registerAction("Add", f, 1);
			controller.registerAction("Factorial", factorial, 2);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testBigGroupExceptions()
	{
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", 1));
		createAndAddInvokers(Arrays.asList(1L), controller);
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Factorial", 1));
	}

	@Test
	public void testBigGroupOneInvokerSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L), controller);
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
	public void testBigGroupOneInvokerSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(2L), controller);
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

	// There is enough ram to execute all functions. (1 ram available, 3 required)
	// Group size should be of 3
	@Test
	public void testBigGroupOneInvokerAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L), controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 3, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 3200 || totalTime < 800) assertTrue(false);
			else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0", str);
	}

	// There is enough ram to execute all functions. (2 ram available, 3 required)
	// Group size should be of 3
	@Test
	public void testBigGroupOneInvokerAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L), controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 3, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 2200 || totalTime < 1800) assertTrue(false);
			else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0", str);
	}

	// There is not enough ram to execute all functions. (2 ram available, 4 required)
	// Group size should be of 2
	@Test
	public void testBigGroupTwoInvokerSameRamSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L, 1L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 1 1", str);
	}

	// There is enough ram to execute all functions. (4 ram available, 4 required)
	// Group size should be of 2
	// It will keep executing in groups of 2 until there is no ram available, 
	// and because it is executed syncronally, there will always be ram available
	@Test
	public void testBigGroupTwoInvokerSameRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(2L, 2L), controller);
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

	// Same distribution of charge because there is not enough ram to execute all functions. (2 ram available, 6 required)
	// Same distribution of charge. Group size should be of 3
	@Test
	public void testBigGroupTwoInvokerSameRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0 1 1 1", str);
	}

	// Same distribution of charge because there is not enough ram to execute all functions. (4 ram available, 6 required)
	// Same distribution of charge. Group size should be of 3
	@Test
	public void testBigGroupTwoInvokerSameRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0 1 1 1", str);
	}

	// Same distribution of charge because there is not enough ram to execute all functions. (3 ram available, 4 required)
	// Group size should be of 2
	@Test
	public void testBigGroupTwoInvokerDifferentRamSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 1 1", str);
	}

	// Only one invoker can execute the function
	@Test
	public void testBigGroupTwoInvokerDifferentRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L), controller);
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1 1", str);
	}

	// Same distribution of charge because there is not enough ram to execute all functions. (3 ram available, 6 required)
	// Group size should be of 3
	@Test
	public void testBigGroupTwoInvokerDifferentRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0 1 1 1", str);
	}

	// Same distribution of charge because there is not enough ram to execute all functions. (3 ram available, 6 required)
	// Group size should be of 3
	@Test
	public void testBigGroupTwoInvokerDifferentRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0 1 1 1", str);
	}

	// There is enough ram to execute all functions. (6 ram available, 6 required)
	// Group size should be of 2
	@Test
	public void testBigGroupTwoInvokerDifferentRamAsyncFuncC()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 4L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 1 1 1 1", str);
	}

	// There is enough ram to execute all functions. (6 ram available, 6 required)
	// Group size should be of 2
	@Test
	public void testBigGroupTwoInvokerDifferentRamAsyncFuncD()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(4L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0 0 1 1", str);
	}

	// There is enough ram to execute all functions. (7 ram available, 6 required)
	// Group size should be of 3
	@Test
	public void testBigGroupTwoInvokerDifferentRamAsyncFuncE()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(4L, 3L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0 1 1 1", str);
	}

	// Only one invoker can execute the function
	@Test
	public void testBigGroupTwoInvokerDifferentRamAsyncFuncF()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 1L), controller);
		initializeSleepAction("Sleep2", 2, controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep2", 2, 1000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
			assertTrue(totalTime < 2300 && totalTime > 800);
		}
		catch (Exception e) {
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep2");
		assertEquals("0 0", str);
	}

	// There is enough ram to execute all functions. (3 ram available, 6 required)
	// Group size should be of 2
	@Test
	public void testBigGroupThreeInvokerSameRamSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L, 1L, 1L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), 
				Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		assertEquals("0 0 1 1 2 2", str);
	}

	// There is enough ram to execute all functions. (6 ram available, 6 required)
	// Group size should be of 2
	// It will keep executing in groups of 2 until there is no ram available, 
	// and because it is executed syncronally, there will always be ram available, so all will be invoked with one invoker
	@Test
	public void testBigGroupThreeInvokerSameRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(2L, 2L, 2L), controller);
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

	// There is enough ram to execute all functions. (3 ram available, 6 required)
	// Group size should be of 2
	@Test
	public void testBigGroupThreeInvokerSameRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 1L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 1 1 2 2", str);
	}

	// There is enough ram to execute all functions. (6 ram available, 6 required)
	// Group size should be of 2
	@Test
	public void testBigGroupThreeInvokerSameRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 2L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 1 1 2 2", str);
	}

	@Test
	public void testBigGroupThreeInvokerDifferentRamSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);
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
	
	// There is not enough ram to execute all functions. (6 ram available, 8 required)
	// Group size should be of 2
	@Test
	public void testBigGroupThreeInvokerDifferentRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);
		
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L, 1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1 1 2 2", str);
	}

	// There is enough ram to execute all functions. (8 ram available, 8 required)
	// Group size should be of 2
	// It will keep executing in groups of 2 until there is no ram available, 
	// and because it is executed syncronally, there will always be ram available
	@Test
	public void testBigGroupThreeInvokerDifferentRamSyncFuncC()
	{
		createAndAddInvokers(Arrays.asList(1L, 3L, 4L), controller);
		
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L, 1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1 1 1 1", str);
	}

	// There is enough ram to execute all functions. (6 ram available, 6 required)
	// Group size should be of 1
	@Test
	public void testBigGroupThreeInvokerDifferentRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(3L, 2L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 0 1 1 2", str);
	}

	// There is enough ram to execute all functions. (6 ram available, 6 required)
	// Group size should be of 1
	@Test
	public void testBigGroupThreeInvokerDifferentRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 1 1 2 2 2", str);
	}

	@Test
	public void testBigGroupThreeInvokerDifferentRamAsyncFuncC()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 3L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 1000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("0 0 1 1 1 2", str);
	}

	@Test
	public void testBigGroupThreeInvokerDifferentRamAsyncFuncD()
	{
		List<String> stringsResult;

		initializeSleepAction("Sleep2", 2, controller);
		createAndAddInvokers(Arrays.asList(2L, 3L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep2", 4, 1000, controller);
			for (String string : stringsResult) {
				System.out.println(string);
			}
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "Sleep2");
		assertEquals("0 0 1 1", str);
	}
	
}
