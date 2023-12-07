package RMI;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import application.Controller;
import faas_exceptions.NoInvokerAvailable;
import invoker.Invoker;
import invoker.InvokerInterface;
import policy_manager.GreedyGroup;
import policy_manager.PolicyManager;

public class ClientController {
	 public static void main(String[] args) {

		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		PolicyManager policyManager = new GreedyGroup();
		controller.addPolicyManager(policyManager);

		SerializedFunction<Map<String, Integer>, Integer> f1 = x -> x.get("x") + x.get("y");
		controller.registerAction("suma", f1, 1);

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
