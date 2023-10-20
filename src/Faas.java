import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Faas {
    public static void main(String[] args) throws Exception {
        Controller<Map<String, Integer>, Integer> controller;
		Integer			result;
		List<Integer>	resultList;

		controller = new Controller<Map<String, Integer>, Integer>();
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		controller.RegisterAction("test", f);

		result = controller.InvokeAction("test", Map.of("x", 1, "y", 2));
		System.out.println(result);
		controller.ListActions();

		List<Map<String, Integer>> input = Arrays.asList(new Map[]{
			Map.of("x", 2, "y", 3),
			Map.of("x", 9, "y", 1),
			Map.of("x", 8, "y", 8),
		});

		resultList = controller.InvokeAction("test", input);
		for (Integer res : resultList) {
			System.out.println(res);
		}
      //  System.out.println("Hello, World!");
    }
}
