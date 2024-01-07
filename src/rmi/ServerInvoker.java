package rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Future;

import core.application.Invokable;
import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.CompositeInvoker;
import core.invoker.Invoker;
import core.invoker.InvokerInterface;
import policymanager.PolicyManager;


public class ServerInvoker extends UnicastRemoteObject implements InvokerInterface {

	private	Invoker	invoker;

	protected ServerInvoker(Long ram, boolean isComposite) throws RemoteException {
		super();
		if (isComposite)
			invoker = CompositeInvoker.createInvoker(ram);
		else
			invoker = Invoker.createInvoker(ram);
	}

	public static void main(String[] args) {
		//creates the struct of the network
		
		ServerInvoker server;
		try {
			//ServerInvoker server = new ServerInvoker(1L);
			server = new ServerInvoker(Long.valueOf(args[1]), Integer.parseInt(args[2]) == 1);

			//Registry registry = LocateRegistry.createRegistry(1099);
			Registry registry = LocateRegistry.createRegistry(Integer.valueOf(args[0]));

			registry.rebind("Invoker", server);

			System.out.println("Invoker with id " + server.getId() + " created and ready.");
		} catch (Exception e) {
			System.err.println("Excepcion del servidor: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public String getId() throws RemoteException {
		return (invoker.getId());
	}

	@Override
	public long	getUsedRam() throws RemoteException {
		return (invoker.getUsedRam());
	}

	@Override
	public long getAvailableRam() throws RemoteException {
		return (invoker.getAvailableRam());
	}

	@Override
	public void reserveRam(long ram) throws RemoteException {
		invoker.reserveRam(ram);
	}

	@Override
	public long getMaxRam() {
		return (invoker.getMaxRam());
	}

	@Override
	public <T, R> R invoke(Invokable<T, R> invokable, T args, String id) throws Exception {
		System.out.println("testing");
		return (invoker.invoke(invokable, args, id));
	}

	@Override
	public <T, R> Future<R> invokeAsync(Invokable<T, R> invokable, T args, String id) throws Exception {
		return (invoker.invokeAsync(invokable, args, id));
	}

	@Override
	public InvokerInterface selectInvoker(long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException {
		return (invoker.selectInvoker(ram));
	}

	@Override
	public void	setPolicyManager(PolicyManager policyManager) throws RemoteException {
		invoker.setPolicyManager(policyManager);
	}

	@Override
	public void shutdownInvoker() {
		invoker.shutdownInvoker();
	}

	//TODO exceptions?
	@Override
	public void registerInvoker(InvokerInterface invoker) throws Exception {
		this.invoker.registerInvoker(invoker);
	}

	@Override
	public void deleteInvoker(InvokerInterface invoker) throws Exception {
		this.invoker.deleteInvoker(invoker);
	}

	@Override
	public void setDistributionPolicyManager(int size, long ram, boolean singleInvocation)
			throws NoInvokerAvailable, RemoteException {
		this.invoker.setDistributionPolicyManager(size, ram, singleInvocation);
	}
}
