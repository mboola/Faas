package RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import invoker.Invoker;

/*
 * This class is different from ServerInvoker. ServerInvoker only creates an invoker but doesn't
 * register invokers to his list.
 * This class will register Invokers to his list.
 */
public class ServerMasterInvoker extends ServerInvoker{

	private	Invoker	invoker;

	protected ServerMasterInvoker() throws RemoteException {
		super();
		invoker = Invoker.createInvoker(1);
	}

	//in
	public static void main(String[] args) {
		try {
			ServerInvoker obj = new ServerInvoker();
			Registry registry = LocateRegistry.createRegistry(Integer.valueOf(args[0]));
			//registry.rebind("Invoker"+obj.invoker.getId(), obj);
			registry.rebind("Invoker", obj);

			System.out.println("Invoker ready.");
		} catch (Exception e) {
			System.err.println("Excepción del servidor: " + e.toString());
			e.printStackTrace();
		}
	}

}
