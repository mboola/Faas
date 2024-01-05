package testing.policy_manager;

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
import core.metrics.MetricSet;
import observer.InvocationObserver;
import policymanager.RoundRobin;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings("unused")
public class TestRoundRobin extends InvocationTester {

	private Controller controller;

	@Before
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();

		Function<Map<String, Integer>, Integer> f = x -> x.get("x") + x.get("y");
		Action factorial = new FactorialAction();

		MetricSet.instantiate().addObserver(new InvocationObserver());

		try {
			controller.setPolicyManager(new RoundRobin());
			initializeSleepAction("Sleep", 1, controller);
			controller.registerAction("Add", f, 1);
			controller.registerAction("Factorial", factorial, 2);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testRoundRobinExceptions()
	{
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", 1));
		createAndAddInvokers(Arrays.asList(1L), controller);
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinOneInvokerAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L), controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 3, 3000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 9200 || totalTime < 8800) assertTrue(false);
			else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("000", str);
	}

	@Test
	public void testRoundRobinOneInvokerAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L), controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep", 3, 3000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;

			if (totalTime > 6200 || totalTime < 5800) assertTrue(false);
			else assertTrue(true);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("101010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerSameRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("101010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamSyncFuncA()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L), controller);
		try {
			List<Integer> result = controller.invoke("Add", 
				Arrays.asList(Map.of("x", 2, "y", 1), Map.of("x", 4, "y", 2), Map.of("x", 7, "y", 3), Map.of("x", 7, "y", 2)));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("1010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L), controller);
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricSet.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("11", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, controller);
		}
		catch (Exception e) {
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("100101", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, controller);
		}
		catch (Exception e) {
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("101010", str);
	}

	@Test
	public void testRoundRobinTwoInvokerDifferentRamAsyncFuncC()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 1L), controller);
		initializeSleepAction("Sleep2", 2, controller);

		try {
			long currentTimeMillis = System.currentTimeMillis();
			stringsResult = invokeList("Sleep2", 2, 4000, controller);
			long totalTime = System.currentTimeMillis() - currentTimeMillis;
			assertTrue(totalTime < 8300 && totalTime > 7800);
		}
		catch (Exception e) {
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep2");
		assertEquals("00", str);
	}

	@Test
	public void testRoundRobinThreeInvokerSameRamSyncFuncA()
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("120120", str);
	}

	@Test
	public void testRoundRobinThreeInvokerSameRamSyncFuncB()
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("120120", str);
	}

	@Test
	public void testRoundRobinThreeInvokerSameRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 1L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("120120", str);
	}

	@Test
	public void testRoundRobinThreeInvokerSameRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 2L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 7000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("120120", str);
	}

	@Test
	public void testRoundRobinThreeInvokerDifferentRamSyncFuncA()
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
		String str = MetricSet.instantiate().getData("InvocationObserver", "Add");
		assertEquals("120120", str);
	}

	@Test
	public void testRoundRobinThreeInvokerDifferentRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);
		
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L, 1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricSet.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1212", str);
	}

	@Test
	public void testRoundRobinThreeInvokerDifferentRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("120122", str);
	}

	@Test
	public void testRoundRobinThreeInvokerDifferentRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(3L, 2L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 10000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("120100", str);
	}

	@Test
	public void testRoundRobinThreeInvokerDifferentRamAsyncFuncC()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 3L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 3000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("120101", str);
	}

	@Test
	public void testRoundRobinThreeInvokerDifferentRamAsyncFuncD()
	{
		List<String> stringsResult;

		initializeSleepAction("Sleep2", 2, controller);
		createAndAddInvokers(Arrays.asList(2L, 3L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep2", 4, 5000, controller);
			for (String string : stringsResult) {
				System.out.println(string);
			}
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep2");
		assertEquals("1010", str);
	}
	
}
