package RMI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import application.Controller;
import invoker.Invoker;
import invoker.InvokerInterface;
import observer.InvocationObserver;
import policy_manager.RoundRobin;

/*
 * This class is different from ServerInvoker. ServerInvoker only creates an invoker but doesn't
 * register invokers to his list.
 * This class will register Invokers to his list.
 */
public class ServerMasterInvoker {

	private static String serializeToString(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(obj);
            byte[] bytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

	public static void main(String[] args) {

		InvocationSet composite = new InvocationSet(Arrays.asList(1L, 1L), null, 1L);
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(2L), Arrays.asList(composite), null);
		
		//System.out.println(controller.toString());

		//here in theory all invokers are registered
		String serializedController = serializeToString(controllerSet);
		String[] argServerHandler = new String[]{serializedController};

		Controller controller = Controller.instantiate();
		SerializedFunction<Integer, Integer> add = x -> x + 1;
		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("add1", add, 1);
		}
		catch (Exception e) {

		}
		Invoker.setController(controller);

		ServerHandler.main(argServerHandler);
		System.out.println("Servers created");

		Invoker.addObserver(new InvocationObserver());

		List<InvokerInterface> invokers = controller.getRegisteredInvokers();
		for (InvokerInterface invokerInterface : invokers) {
			System.out.println("a");
		}

		try {
			Integer result = controller.invoke("add1", 10);
			System.out.println(result);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		/*
		 * try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("add1", add, 1);

			List<Integer> result = controller.invoke("add1", Arrays.asList(2, 5, 19, 10, 9));
			for (Integer res : result) {
				System.out.println(res);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

		String str = controller.getData("InvocationObserver", "add1");
		System.out.println(str);
		 */
	}

}
