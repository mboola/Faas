package rmi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;

import core.application.Controller;
import core.metrics.MetricCollection;
import observer.InvocationObserver;
import policymanager.RoundRobin;

/**
 * MainRmi class serves as the entry point for the Remote Method Invocation (RMI) demonstration.
 * It initializes a Controller, registers an action, creates and serializes an InvocationSet containing information about invokers,
 * starts a server using ServerHandler to simulate the invocation environment,
 * performs an invocation through the Controller, and prints the result along with collected metrics.
 * 
 * @see Controller
 * @see SerializedFunction
 * @see InvocationSet
 * @see ServerHandler
 * @see MetricCollection
 */
public class MainRmi {

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

		// Here we define the Controller without his invokers
		Controller controller = Controller.instantiate();
		SerializedFunction<Integer, Integer> add = x -> x + 1;
		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("Add", add, 1);
			MetricCollection.instantiate().addObserver(new InvocationObserver());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Here we define the struct of Invokers and CompositeInvokers
		InvocationSet composite = new InvocationSet(Arrays.asList(1L, 1L), null, 1L);
		InvocationSet controllerSet = new InvocationSet(Arrays.asList(2L), Arrays.asList(composite), null);

		// And we serialize all the data needed to create the servers and create them
		String serializedControllerInformation = serializeToString(controllerSet);
		ServerHandler.main(new String[]{serializedControllerInformation});

		// We try to invoke a simple function that adds one to the input
		try {
			Integer result = controller.invoke("Add", 10);
			System.out.println(result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// And here we check if observers did their job
		String str = MetricCollection.instantiate().getData("InvocationObserver", "Add");
		System.out.println(str);
	}

}
