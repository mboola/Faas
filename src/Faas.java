import java.util.Map;
import java.util.function.Function;

public class Faas {
    public static void main(String[] args) throws Exception {
        Controller<Map<String, Integer>, Integer> controller;
		Integer		result;

		controller = new Controller<Map<String, Integer>, Integer>();
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		controller.RegisterAction("test", f);

		result = controller.InvokeAction("test", Map.of("x", 1, "y", 2));
		System.out.println(result);
		controller.ListActions();
      //  System.out.println("Hello, World!");
    }
}
