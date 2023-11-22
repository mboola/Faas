package RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Future;

import application.Action;
import application.Invoker;

public class ServerInvoker extends UnicastRemoteObject implements InvokerInterface {

	private	Invoker	invoker;

	protected ServerInvoker() throws RemoteException {
		super();
		invoker = Invoker.createInvoker(1);
	}

	public static void main(String[] args) {
		try {
			ServerInvoker obj = new ServerInvoker();
			Registry registry = LocateRegistry.createRegistry(1099);
			//registry.rebind("Invoker"+obj.invoker.getId(), obj);
			registry.rebind("Invoker", obj);

			System.out.println("Invoker ready.");
		} catch (Exception e) {
			System.err.println("Excepción del servidor: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public String getId() {
		return (invoker.getId());
	}

	@Override
	public long getRamUsed() {
		return (invoker.getRamUsed());
	}

	@Override
	public long getAvailableRam() {
		return (invoker.getAvailableRam());
	}

	@Override
	public long getMaxRam() {
		return (invoker.getMaxRam());
	}

	@Override
	public <T, R> R invoke(Action action, T args, String id) throws Exception {
		System.out.println("testing");
		return (invoker.invoke(action, args, id));
	}

	@Override
	public <T, R> Future<R> invokeAsync(Action action, T args, String id) throws Exception {
		return (invoker.invoke(action, args, id));
	}

	@Override
	public void shutdownInvoker() {
		invoker.shutdownInvoker();
	}
}
