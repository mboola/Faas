import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Faas {
    public static void main(String[] args) throws Exception {
        Controller		controller;
		Integer			result;
		List<Object>	resultList;
		Invoker			invoker1, invoker2;
		PolicyManager	policyManager;

		controller = Controller.instantiate();
		Invoker.addObserver(new TimerObserver());
		Invoker.setController(controller);
		
		invoker1 = new Invoker(2);
		invoker2 = new Invoker(2);
		controller.registerInvoker(invoker1);
		controller.registerInvoker(invoker2);
		policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);
		Function<Map<String, Integer>, Integer> f1 = x -> x.get("x") - x.get("y");
		controller.registerAction("sub", f1, 2);
		
		try {
			result = (Integer) controller.invoke("sub", Map.of("x", 1, "y", 2));
			System.out.println(result);
		}
		catch (NoInvokerAvaiable e1) {
			System.out.println(e1.getMessage());
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
			resultList = controller.invoke("sub", input);
			for (Object res : resultList) {
				System.out.println((Integer)res);
			}
		}
		catch (NoInvokerAvaiable e1) {
			System.out.println(e1.getMessage());
		}
		System.out.println("Start of time.");
		controller.showTime("sub");
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
			for(int i = 0; i < 6; i++)
			{
				fut = controller.invoke_async("sleepAction", 5);
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
		catch (NoInvokerAvaiable e1) {
			System.out.println(e1.getMessage());
		}

		Invoker.printCache();

		controller.shutdownAllInvokers();
    }
}
