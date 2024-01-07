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
import policymanager.UniformGroup;
import policymanager.UniformGroup;
import services.otheractions.FactorialAction;
import testing.InvocationTester;

@SuppressWarnings("unused")
public class TestUniformGroup extends InvocationTester {

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
			controller.setPolicyManager(new UniformGroup());
			initializeSleepAction("Sleep", 1, controller);
			controller.registerAction("Add", f, 1);
			controller.registerAction("Factorial", factorial, 2);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	//TODO: infinite loop for some reason
	@Test
	public void testUniformGroupExceptions()
	{
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Add", 1));
		createAndAddInvokers(Arrays.asList(1L), controller);
		assertThrows(NoInvokerAvailable.class, () -> controller.invoke("Factorial", 1));
	}

	@Test
	public void testUniformGrouOneInvokerSyncFuncA()
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
	public void testUniformGroupOneInvokerSyncFuncB()
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
	public void testUniformGroupOneInvokerAsyncFuncA()
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
	public void testUniformGroupOneInvokerAsyncFuncB()
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
	public void testUniformGroupTwoInvokerSameRamSyncFuncA()
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
		assertEquals("0011", str);
	}

	@Test
	public void testUniformGroupTwoInvokerSameRamSyncFuncB()
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
		assertEquals("0011", str);
	}

	@Test
	public void testUniformGroupTwoInvokerSameRamAsyncFuncA()
	{
		List<String> stringsResult;

		createAndAddInvokers(Arrays.asList(1L, 1L), controller);

		try {
			stringsResult = invokeList("Sleep", 6, 6000, controller);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		String str = MetricSet.instantiate().getData("InvocationObserver", "Sleep");
		assertEquals("000111", str);
	}

	@Test
	public void testUniformGroupTwoInvokerSameRamAsyncFuncB()
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
		assertEquals("000111", str);
	}

	@Test
	public void testUniformGroupTwoInvokerDifferentRamSyncFuncA()
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
		assertEquals("0011", str);
	}

	//error for some reason TODO
	@Test
	public void testUniformGroupTwoInvokerDifferentRamSyncFuncB()
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
	public void testUniformGroupTwoInvokerDifferentRamAsyncFuncA()
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
		assertEquals("000111", str);
	}

	@Test
	public void testUniformGroupTwoInvokerDifferentRamAsyncFuncB()
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
		assertEquals("000111", str);
	}

	@Test
	public void testUniformGroupTwoInvokerDifferentRamAsyncFuncC()
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
	public void testUniformGroupThreeInvokerSameRamSyncFuncA()
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
		assertEquals("001122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerSameRamSyncFuncB()
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
		assertEquals("001122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerSameRamAsyncFuncA()
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
		assertEquals("001122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerSameRamAsyncFuncB()
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
	public void testUniformGroupThreeInvokerDifferentRamSyncFuncA()
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
		assertEquals("001122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerDifferentRamSyncFuncB()
	{
		createAndAddInvokers(Arrays.asList(1L, 2L, 3L), controller);
		
		try {
			List<Long> result = controller.invoke("Factorial", Arrays.asList(1L, 1L, 1L, 1L));
		}
		catch (Exception e){
			assertTrue(false);
		}
		String str = MetricSet.instantiate().getData("InvocationObserver", "Factorial");
		assertEquals("1122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerDifferentRamAsyncFuncA()
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
		assertEquals("001122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerDifferentRamAsyncFuncB()
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
		assertEquals("001122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerDifferentRamAsyncFuncC()
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
		assertEquals("001122", str);
	}

	@Test
	public void testUniformGroupThreeInvokerDifferentRamAsyncFuncD()
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
		assertEquals("0011", str);
	}
	
}
