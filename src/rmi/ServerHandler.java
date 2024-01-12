package rmi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Base64;

import core.application.Controller;
import core.invoker.InvokerInterface;

/**
 * Class used to define multiple invokers as a servers.
 * This desirializes the information on 
 */
public class ServerHandler {

	public static int portStart = 1099;

	private static int createInvokerServers(InvokerInterface composite, InvocationSet parentInvocator, Integer currentPort)
	{
		Registry registry;
		InvokerInterface stub;
		String[] argsInvoker = new String[3];

		//first create servers with invokers and then add them to the controller
		for (Long ram : parentInvocator.getInvokers()) {
			argsInvoker[0] = ((Integer)currentPort).toString();
			argsInvoker[1] = ram.toString();
			argsInvoker[2] = "0";
			ServerInvoker.main(argsInvoker);

			try {
				registry = LocateRegistry.getRegistry("localhost", currentPort);
				stub = (InvokerInterface) registry.lookup("Invoker");
				composite.registerInvoker(stub);
			}
			catch (Exception e) {
				System.err.println("Excepción del cliente: " + e.toString());
				e.printStackTrace();
			}
			currentPort++;
		}

		if (parentInvocator.getComposites() == null)
			return (currentPort);

		for (InvocationSet currentComposite : parentInvocator.getComposites())
		{
			argsInvoker[0] = ((Integer)currentPort).toString();
			argsInvoker[1] =  currentComposite.getRam().toString();
			argsInvoker[2] = "1";
			ServerInvoker.main(argsInvoker);

			try {
				//I create the server of InvokerComposite and add it to the controller
				registry = LocateRegistry.getRegistry("localhost", currentPort);
				stub = (InvokerInterface) registry.lookup("Invoker");
				composite.registerInvoker(stub);
				currentPort++;
				currentPort = createInvokerServers(stub, currentComposite, currentPort);
			}
			catch (Exception e) {
				System.err.println("Excepción del cliente: " + e.toString());
				e.printStackTrace();
			}
		}

		return (currentPort);
	}

	public static void main(String[] args) {

		System.out.println("ServerHandler working.");
		//here we get the serialized InvocationSet in ServerMasterInvoker and deserialize it
		InvocationSet controllerSet = null;

		try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(args[0]))))
		{
			controllerSet = (InvocationSet) inputStream.readObject();
			System.out.println("Object deserialized successfully.");
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (controllerSet == null)
			return ;
		
		int currentPort = portStart;
		String[] argsInvoker = new String[3];
		Registry registry;
		InvokerInterface stub;
		Controller controller = Controller.instantiate();
		
		//here I add all invokers to controller
		for (Long ram : controllerSet.getInvokers()) {
			argsInvoker[0] = ((Integer)currentPort).toString();
			argsInvoker[1] = ram.toString();
			argsInvoker[2] = "0";
			ServerInvoker.main(argsInvoker);

			try {
				registry = LocateRegistry.getRegistry("localhost", currentPort);
				stub = (InvokerInterface) registry.lookup("Invoker");
				controller.registerInvoker(stub);
			}
			catch (Exception e) {
				System.err.println("Excepción del cliente: " + e.toString());
				e.printStackTrace();
			}
			currentPort++;
		}

		//and here for each composite I create all the servers needed.
		if (controllerSet.getComposites() == null)
			return ;
		for (InvocationSet currentComposite : controllerSet.getComposites())
		{
			argsInvoker[0] = ((Integer)currentPort).toString();
			argsInvoker[1] =  currentComposite.getRam().toString();
			argsInvoker[2] = "1";
			ServerInvoker.main(argsInvoker);

			try {
				//I create the server of InvokerComposite and add it to the controller
				registry = LocateRegistry.getRegistry("localhost", currentPort);
				stub = (InvokerInterface) registry.lookup("Invoker");
				controller.registerInvoker(stub);
				currentPort++;
				currentPort = createInvokerServers(stub, currentComposite, currentPort);
			}
			catch (Exception e) {
				System.err.println("Excepción del cliente: " + e.toString());
				e.printStackTrace();
			}
		}

		//here in theory all invokers are registered
	}
}
