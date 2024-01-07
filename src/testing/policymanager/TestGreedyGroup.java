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
import core.metrics.MetricSet;
import observer.InvocationObserver;
import policymanager.GreedyGroup;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings("unused")
public class TestGreedyGroup extends InvocationTester {

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
			controller.setPolicyManager(new GreedyGroup());
			initializeSleepAction("Sleep", 1, controller);
			controller.registerAction("Add", f, 1);
			controller.registerAction("Factorial", factorial, 2);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testGreedyGroupExceptions()
	{
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", 1));
		createAndAddInvokers(Arrays.asList(1L), controller);
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Factorial", 1));
	}

	@Test
	public void testGreedyGroupOneInvokerSyncFuncA()
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
	public void testGreedyGroupOneInvokerSyncFuncB()
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
	public void testGreedyGroupOneInvokerAsyncFuncA()
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
	public void testGreedyGroupOneInvokerAsyncFuncB()
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
	public void testGreedyGroupTwoInvokerSameRamSyncFuncA()
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
		assertEquals("0000", str);
	}

	@Test
	public void testGreedyGroupTwoInvokerSameRamSyncFuncB()
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
		assertEquals("0000", str);
	}

	@Test
	public void testGreedyGroupTwoInvokerSameRamAsyncFuncA()
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
		assertEquals("011010", str);
	}

	@Test
	public void testGreedyGroupTwoInvokerSameRamAsyncFuncB()
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
		assertEquals("001110", str);
	}

	@Test
	public void testGreedyGroupTwoInvokerDifferentRamSyncFuncA()
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
		assertEquals("0000", str);
	}

	@Test
	public void testGreedyGroupTwoInvokerDifferentRamSyncFuncB()
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
	public void testGreedyGroupTwoInvokerDifferentRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 6000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("100101", str);
	}

	@Test
	public void testGreedyGroupTwoInvokerDifferentRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 6000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("011101", str);
	}

	@Test
	public void testGreedyGroupTwoInvokerDifferentRamAsyncFuncC()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(2L, 1L), controller);
		initializeSleepAction("Sleep2", 2, controller);

		try {
			stringsResult = invokeList("Sleep2", 2, 2000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep2");
		assertEquals("00", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerSameRamSyncFuncA()
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
		assertEquals("000000", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerSameRamSyncFuncB()
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
		assertEquals("000000", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerSameRamAsyncFuncA()
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
		assertEquals("012120", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerSameRamAsyncFuncB()
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
		assertEquals("001122", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerDifferentRamSyncFuncA()
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
		assertEquals("000000", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerDifferentRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);
		
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L, 1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricSet.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1111", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerDifferentRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 6000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("011222", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerDifferentRamAsyncFuncB()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(3L, 2L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 6000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("211000", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerDifferentRamAsyncFuncC()
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
		assertEquals("200111", str);
	}

	@Test
	public void testGreedyGroupThreeInvokerDifferentRamAsyncFuncD()
	{
		List<String> stringsResult;

		initializeSleepAction("Sleep2", 2, controller);
		createAndAddInvokers(Arrays.asList(2L, 3L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep2", 4, 5000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep2");
		assertEquals("0110", str);
	}
	
}
