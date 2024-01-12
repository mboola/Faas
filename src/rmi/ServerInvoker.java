package rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Future;

import core.application.Invokable;
import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.exceptions.OperationNotValid;
import core.invoker.CompositeInvoker;
import core.invoker.Invoker;
import core.invoker.InvokerInterface;
import policymanager.PolicyManager;

/**
 * The ServerInvoker class represents a remote server that hosts an Invoker, which is responsible for
 * executing functions and managing available resources.
 * <p>
 * This class extends the {@link UnicastRemoteObject} and implements the {@link core.invoker.InvokerInterface} for RMI communication.
 */
public class ServerInvoker extends UnicastRemoteObject implements InvokerInterface {

	private	Invoker	invoker;

	/**
     * Constructs a ServerInvoker instance based on the specified parameters.
     * 
     * @param ram          The maximum RAM of the Invoker.
     * @param isComposite  Indicates whether the Invoker is a CompositeInvoker.
     * @throws RemoteException If a communication-related exception occurs.
     */
	protected ServerInvoker(Long ram, int threads, boolean isComposite) throws RemoteException {
		super();
		if (isComposite)
			invoker = CompositeInvoker.createInvoker(ram, threads);
		else
			invoker = Invoker.createInvoker(ram, threads);
	}

	/**
     * Main method to create and bind a ServerInvoker instance to an RMI registry.
     * 
     * @param args The command-line arguments containing the port, RAM, and type of Invoker.
     */
	public static void main(String[] args) {
		
		ServerInvoker server;
		int threads = 4;
		try {
			server = new ServerInvoker(Long.valueOf(args[1]), threads, Integer.parseInt(args[2]) == 1);

			Registry registry = LocateRegistry.createRegistry(Integer.valueOf(args[0]));

			registry.rebind("Invoker", server);

			System.out.println("Invoker with id " + server.getId() + " created and ready.");
		} catch (Exception e) {
			System.err.println("Excepcion del servidor.");
			e.printStackTrace();
		}
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public String getId() throws RemoteException {
		return (invoker.getId());
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public long	getUsedRam() throws RemoteException {
		return (invoker.getUsedRam());
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public long getAvailableRam() throws RemoteException {
		return (invoker.getAvailableRam());
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void reserveRam(long ram) throws RemoteException {
		invoker.reserveRam(ram);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public long getMaxRam() throws RemoteException {
		return (invoker.getMaxRam());
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public <T, R> R invoke(Invokable<T, R> invokable, T args, String id) throws Exception {
		return (invoker.invoke(invokable, args, id));
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public <T, R> Future<R> invokeAsync(Invokable<T, R> invokable, T args, String id) throws Exception {
		return (invoker.invokeAsync(invokable, args, id));
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public InvokerInterface selectInvoker(long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException {
		return (invoker.selectInvoker(ram));
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void	setPolicyManager(PolicyManager policyManager) throws RemoteException {
		invoker.setPolicyManager(policyManager);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void shutdownInvoker() throws RemoteException {
		invoker.shutdownInvoker();
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void registerInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException {
		this.invoker.registerInvoker(invoker);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void deleteInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException {
		this.invoker.deleteInvoker(invoker);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void setDistributionPolicyManager(int size, long ram) throws NoInvokerAvailable, RemoteException {
		this.invoker.setDistributionPolicyManager(size, ram);
	}
}
