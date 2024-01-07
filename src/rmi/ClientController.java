package rmi;

import java.io.NotSerializableException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import core.application.Controller;
import core.exceptions.NoInvokerAvailable;
import core.invoker.InvokerInterface;
import policymanager.RoundRobin;

/**
 * This class will locate all servers that have an invoker ready and will use them to
 * invoke functions. If
 */
public class ClientController {
	public static void main(String[] args) {

		Controller controller = Controller.instantiate();

		SerializedFunction<Map<String, Integer>, Integer> f1 = x -> x.get("x") + x.get("y");
		try {
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("suma", f1, 1);
		}
		catch (Exception e) {

		}

		//here get all invokers and add them to contoller
		try {
			for (int i = 0; i < 1; i++)
			{
				//maybe change the port or something
				System.out.println("Trying to locate Invoker " + ((Integer)i).toString());

				Registry registry = LocateRegistry.getRegistry("localhost", 1099);
				//InvokerInterface stub = (InvokerInterface) registry.lookup("Invoker"+((Integer)i).toString());
				InvokerInterface stub = (InvokerInterface) registry.lookup("Invoker");
				System.out.println("Invoker " + ((Integer)i).toString() + " found.");
				System.out.println("Trying register Invoker " + ((Integer)i).toString() + " into controller.");
				controller.registerInvoker(stub);
				System.out.println("Invoker " + ((Integer)i).toString() + " registered.");
			}
		} catch (Exception e) {
			System.err.println("Excepción del cliente: " + e.toString());
			e.printStackTrace();
		}

		try {
			int result = (Integer) controller.invoke("suma", Map.of("x", 1, "y", 2));
			System.out.println(result);
		}
		catch (NoInvokerAvailable e) {
			System.out.println(e.getMessage());
		}
		catch (NotSerializableException e) {
			System.out.println(e.getMessage());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
