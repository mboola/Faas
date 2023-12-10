package testing;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.Test;

import application.Controller;
import faas_exceptions.NoInvokerAvailable;
import invoker.Invoker;
import policy_manager.GreedyGroup;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class PolicyManagerTest {

	@Test
	public void	testRoundRobin()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);

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
			controller.registerInvoker(invoker);
			controller.registerAction("sleepAction", sleep, 1);
		}
		catch (Exception e) {
			assertTrue(false);
		}

		/* 
		 * This test consists in creating threads and observing if the execution
		 * time we got is the same as expected.
		 */

		// In this case we create two threads of 2 sec each and that each execution will
		// consume 1MB. We only have one invoker with 1MB, so in theory execution time will be
		// of 4 seconds in total.

		int	err = 0;
		long currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 2; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		long totalTime = System.currentTimeMillis() - currentTimeMillis;

		int result;
		if (totalTime > 4500 || totalTime < 4000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);

		// We add another invoker of 1MB. Now we will test 4 threads that consume 1MB
		// and the time execution is of 2 sec each. We will have 2 invokers of 1MB each, so the
		// expected time should be also of 4 sec in total
		Invoker invoker2 = Invoker.createInvoker(1);
		controller.registerInvoker(invoker2);

		err = 0;
		currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 4; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		totalTime = System.currentTimeMillis() - currentTimeMillis;

		if (totalTime > 4500 || totalTime < 4000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);

		// Now we will test with an odd number of threads: 5. The expected time should be 6 sec
		currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 5; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		totalTime = System.currentTimeMillis() - currentTimeMillis;

		if (totalTime > 6500 || totalTime < 6000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);

		// Now we will test with an odd number of invokers and an even number of threads
		// The execution time expected is of 4 seconds.
		Invoker invoker3 = Invoker.createInvoker(1);
		controller.registerInvoker(invoker3);

		currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 4; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		totalTime = System.currentTimeMillis() - currentTimeMillis;

		if (totalTime > 4500 || totalTime < 4000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);

		// Now we will test with an odd number of invokers and an odd number of threads
		// The execution time expected is of 2 seconds.

		currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 3; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		totalTime = System.currentTimeMillis() - currentTimeMillis;

		if (totalTime > 2500 || totalTime < 2000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);
	}

	@Test
	public void	testGreedyGroup()
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new GreedyGroup();
		controller.addPolicyManager(policyManager);

		Function<Integer, String> sleep = s -> {
			try {
				Thread.sleep(Duration.ofSeconds(s).toMillis());
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
		controller.registerAction("sleepAction", sleep, 1);

		// BASE CASE
		// In this case we create two threads of 2 sec each and that each execution will
		// consume 1MB. We only have one invoker with 1MB, so in theory execution time will be
		// of 4 seconds in total.

		int	err = 0;
		long currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 2; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		long totalTime = System.currentTimeMillis() - currentTimeMillis;

		int result;
		if (totalTime > 4500 || totalTime < 4000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);

		// We add another invoker of 1MB. Now we will test 4 threads that consume 1MB
		// and the time execution is of 2 sec each. We will have 2 invokers of 1MB each, so the
		// expected time should be also of 4 sec in total
		Invoker invoker2 = Invoker.createInvoker(1);
		controller.registerInvoker(invoker2);

		err = 0;
		currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 4; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		totalTime = System.currentTimeMillis() - currentTimeMillis;

		if (totalTime > 4500 || totalTime < 4000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);
		 

		// Now we delete the registered invokers and test with invokers with more capacity.
		// We will have 2 invokers of 2MB each and we will run 2 threads. The execution time expected
		// is of 2 seconds, and only one invoker should be used.

		controller.deleteInvoker(invoker);
		controller.deleteInvoker(invoker2);

		Invoker invoker3 = Invoker.createInvoker(2);
		controller.registerInvoker(invoker3);
		Invoker invoker4 = Invoker.createInvoker(2);
		controller.registerInvoker(invoker4);

		err = 0;
		currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 2; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
			}
			List<String> stringsResult = new LinkedList<String>();
			for (Future<String> future : resList) {
				stringsResult.add(future.get());
			}
		} catch (NoInvokerAvailable e1) {
			err = 1;
		} catch (Exception e) {
			err = 2;
		}
		totalTime = System.currentTimeMillis() - currentTimeMillis;

		if (totalTime > 2500 || totalTime < 2000)
			result = 0;
		else
			result = 1;
		assertEquals(result, 1);
		assertEquals(err, 0);

		//TODO: create a method to know the last time an invocator was used
		//we need to know if this works correctly or not
	}
}
