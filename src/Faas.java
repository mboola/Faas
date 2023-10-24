import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Faas {
    public static void main(String[] args) throws Exception {
        Controller		controller;
		Integer			result;
		List<Object>	resultList;
		Invoker			invoker1;

		controller = Controller.instantiate();
		invoker1 = new Invoker(3);
		controller.registerInvoker(invoker1);
		Function<Map<String, Integer>, Integer> f1 = x -> x.get("x") - x.get("y");
		controller.registerAction("sub", f1);

		result = (Integer) controller.invoke("sub", Map.of("x", 1, "y", 2));
		System.out.println(result);
		controller.listActions();

		List<Object> input = Arrays.asList(
			Map.of("x", 2, "y", 3),
			Map.of("x", 9, "y", 1),
			Map.of("x", 8, "y", 8)
		);

		resultList = controller.invoke("sub", input);
		for (Object res : resultList) {
			System.out.println((Integer)res);
		}

		//test async
		Function<Integer, String> sleep = s -> {
			try {
				Thread.sleep(Duration.ofSeconds(s).toMillis());
				return "Done!";
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};

		controller.registerAction("sleepAction", sleep);
		long currentTimeMillis = System.currentTimeMillis();
		Future<String> fut1 = controller.invoke_async("sleepAction", 5);
		Future<String> fut2 = controller.invoke_async("sleepAction", 5);
		Future<String> fut3 = controller.invoke_async("sleepAction", 5);
		String s1 = fut1.get();
		String s2 = fut2.get();
		String s3 = fut3.get();
		long finalimeMillis = System.currentTimeMillis();
		System.out.println(s1 + s2 + s3 + " Seconds:" + (finalimeMillis - currentTimeMillis));

		controller.shutdownAllInvokers();
    }
}
