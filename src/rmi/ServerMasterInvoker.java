package rmi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import core.application.Controller;
import core.invoker.InvokerInterface;
import core.metrics.MetricCollection;
import observer.InvocationObserver;
import policymanager.RoundRobin;

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

		ServerHandler.main(argServerHandler);
		System.out.println("Servers created");

		List<InvokerInterface> invokers = controller.getRegisteredInvokers();

		try {
			MetricCollection.instantiate().addObserver(new InvocationObserver());
			Integer result = controller.invoke("add1", 10);
			System.out.println(result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		String str = MetricCollection.instantiate().getData("InvocationObserver", "add1");
		System.out.println(str);
	}

}
