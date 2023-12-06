package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import decorator.CacheDecorator;
import decorator.TimerDecorator;
import faas_exceptions.NoInvokerAvailable;
import faas_exceptions.OperationNotValid;
import invoker.Invoker;
import invoker.InvokerComposite;
import observer.IdObserver;
import policy_manager.GreedyGroup;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class Faas {
	public static void main(String[] args) 
	{
		Controller controller = Controller.instantiate();
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);
		Invoker.setController(controller);
		InvokerComposite invoker = InvokerComposite.createInvoker(1);
		Invoker invokerSimple0 = Invoker.createInvoker(1);
		Invoker.addObserver(new IdObserver());
		try {
			controller.registerInvoker(invoker);
			controller.registerInvoker(invokerSimple0);
		} catch (OperationNotValid e) {
			System.out.println("error registering.\n");
		}

		Invoker invokerSimple1 = Invoker.createInvoker(2);
		Invoker invokerSimple2 = Invoker.createInvoker(3);
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		try
		{
			invoker.registerInvoker(invokerSimple1);
			invoker.registerInvoker(invokerSimple2);
			//controller.registerInvoker(invokerSimple1);
			controller.registerAction("sub", f, 1);
			Integer result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			//get invoker used
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			//get invoker used
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			//get invoker used
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			String str = controller.metrics.getData("IdObserver", "sub");
			System.out.println(str);
		}
		catch (Exception e){
			System.out.println("error executing.\n");
		}

		/*
		 * Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);
		Function<Map<String, Integer>, Integer> f2 = x -> x.get("x") - x.get("y");
		controller.registerAction("sub2", f2, 2);

		 Invoking a function with not enough RAM 
		int result = 0;
		int err = 0;
		try {
			result = (Integer) controller.invoke("sub2", Map.of("x", 2, "y", 1));
		} catch (NoInvokerAvaiable e1) {
			err = 1;
		} catch (NoResultAvaiable e2) {
			err = 2;
		} catch (Exception e3) {
			err = 3;
		}
		System.out.println("Result: " + result);
		System.out.println("Err: " + err);
		 */
		
		/*Controller controller = Controller.instantiate();
		//Invoker.addObserver(new TimerObserver());
		Invoker.setController(controller);
		Invoker invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker);
		Function<Action, Function<Object,Object>> decoratorInitializer = 
			(action) -> {
				Function<Object, Object>	timerDecorator;
				Function<Object, Object>	cacheDecorator;
				String						id;
				Function<Object, Object>	function;

				function = (Function<Object, Object>)action.getFunction();
				id = action.getId();
				cacheDecorator = new CacheDecorator<>(function, id);
				timerDecorator = new TimerDecorator<>(cacheDecorator);

				return (timerDecorator);
			}
		;
		Invoker.setDecoratorInitializer(decoratorInitializer);
		//Invoker invoker2 = Invoker.createInvoker(2);
		//controller.registerInvoker(invoker2);
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);

		Function<Map<String, Integer>, Integer> f1 = x -> x.get("x") + x.get("y");
		controller.registerAction("suma", f1, 1);

		try {
			int result = (Integer) controller.invoke("suma", Map.of("x", 1, "y", 2));
			System.out.println(result);
		}
		catch (NoInvokerAvailable e1) {
			System.out.println(e1.getMessage());
		}
		catch (Exception e2) {
			System.out.println(e2.getMessage());
		}
		controller.listActions();

		List<Object> input = Arrays.asList(
			Map.of("x", 2, "y", 3),
			Map.of("x", 9, "y", 1),
			Map.of("x", 8, "y", 8),
			Map.of("x", 100000000, "y", -10),
			Map.of("x", 1, "y", 8123),
			Map.of("x", 2, "y", 4418),
			Map.of("x", 85, "y", 312348),
			Map.of("x", 812312312, "y", -4444128)
		);

		try {
			List<Integer> resultList = controller.invoke("sub", input);
			for (Object res : resultList) {
				System.out.println((Integer)res);
			}
		}
		catch (NoInvokerAvailable e1) {
			System.out.println(e1.getMessage());
		}
		catch (Exception e2) {
			System.out.println(e2.getMessage());
		}
		System.out.println("Start of time.");
		//controller.metrics.showTime("sub");
		System.out.println("End of time.");

		//test async
		Function<Integer, String> sleep = s -> {
			try {
				Thread.sleep(Duration.ofSeconds(s).toMillis());
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};

		controller.registerAction("sleepAction", sleep, 1);
		try {
			long currentTimeMillis = System.currentTimeMillis();
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
			for (String str : stringsResult) {
				System.out.println(str);
			}
			long finalimeMillis = System.currentTimeMillis();
			System.out.println(" Seconds:" + (finalimeMillis - currentTimeMillis));
		}
		catch (NoInvokerAvailable e1) {
			System.out.println(e1.getMessage());
		}
		catch (Exception e2) {
			System.out.println(e2.getMessage());
		}

		Invoker.printCache();

		controller.shutdownAllInvokers();*/

		/*
		 * Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		//Invoker invoker = Invoker.createInvoker(1);
		//controller.registerInvoker(invoker);
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

		Invoker invoker3 = Invoker.createInvoker(2);
		controller.registerInvoker(invoker3);
		Invoker invoker4 = Invoker.createInvoker(2);
		controller.registerInvoker(invoker4);

		int err = 0;
		long currentTimeMillis = System.currentTimeMillis();
		try {
			Future<String> fut;
			List<Future<String>> resList = new LinkedList<Future<String>>();
			for(int i = 0; i < 2; i++)
			{
				fut = controller.invoke_async("sleepAction", 2);
				resList.add(fut);
				controller.listInvokersRam();
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
		System.out.println(totalTime);

		controller.shutdownAllInvokers();
		 */

	}
}