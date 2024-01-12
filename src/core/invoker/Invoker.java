package core.invoker;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import core.application.Invokable;
import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.exceptions.OperationNotValid;
import core.metrics.MetricRecollector;
import policymanager.PolicyManager;

/**
 * The {@code Invoker} class represents a computing resource capable of executing functions with specified RAM requirements.
 * It implements the {@link InvokerInterface} interface and provides functionality for synchronous and asynchronous function invocation.
 * Each instance of the Invoker class is associated with a unique identifier, maximum RAM capacity, and usage statistics.
 *
 * <p><strong>Note:</strong> The constructor is protected to ensure instances are created using the {@link #createInvoker(long, int)} method.</p>
 *
 * @see Invokable
 * @see MetricRecollector
 * @see PolicyManager
 */
public class Invoker implements InvokerInterface, Serializable {

	/** The ExecutorService responsible for managing the threads used by the Invoker for asynchronous function execution. */
	private final transient ExecutorService executor;

	/** A counter to keep track of the total number of Invoker instances created. */
	private static long numInvokers = 0;

	/** The unique identifier for the Invoker instance. */
	private final String id;
	
	/**
	 * The maximum amount of RAM that this Invoker can use to execute functions.
	 * Note: If an Invokable function requires more RAM than specified here, it cannot be executed on this Invoker.
	 */
	private final long maxRam;

	/** The amount of RAM in use the invoker has to outside classes. */
	private long reservedRam;

	/** The current amount of RAM actively in use by the Invoker. */
	private long usedRam;

	/**
	 * Constructs a new instance of the Invoker.
	 *
	 * @param ram     The maximum RAM capacity of the Invoker.
	 * @param threads The number of threads to be used by the Invoker's executor service.
	 */
	protected Invoker(long ram, int threads){
		int cores = Runtime.getRuntime().availableProcessors();

		if (threads > cores)
			executor = Executors.newFixedThreadPool(cores);
		else
			executor = Executors.newFixedThreadPool(threads);

		this.maxRam = ram;
		usedRam = 0;
		reservedRam = 0;

		id = ((Long)numInvokers).toString();
		numInvokers++;
	}

	/**
	 * Creates an Invoker with the specified RAM capacity and thread count.
	 *
	 * @param ram     The maximum RAM capacity of the Invoker.
	 * @param threads The number of threads to be used by the Invoker's executor service.
	 * @return The created Invoker instance, or null if the RAM or thread count is less than or equal to zero.
	 */
	public static Invoker createInvoker(long ram, int threads) {
		if (ram <= 0 || threads <= 0)
			return (null);
		else
			return new Invoker(ram, threads);
	}
	
	/**
	 * Returns the unique identifier of the Invoker.
	 *
	 * @return The Invoker's identifier.
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Returns the amount of RAM currently in use by the Invoker.
	 *
	 * @return The used RAM.
	 */
	@Override
	public long	getUsedRam()
	{
		return usedRam;
	}

	/**
	 * Returns the available RAM that can be reserved for function execution. This method is accessed by the policy manager
	 * to distribute the invocation load. It represents the difference between the maximum RAM allowed (maxRam) and the
	 * currently reserved RAM (reservedRam). To get actual ram being used by the Invoker, refeer to {@link Invoker#getUsedRam()}.
	 *
	 * @return The available RAM that can be reserved for execution.
	 */
	@Override
	public long getAvailableRam()
	{
		return maxRam - reservedRam;
	}

	/**
	 * Reserves a specified amount of RAM for execution.
	 *
	 * @param ram The amount of RAM to reserve.
	 */
	@Override
	public void reserveRam(long ram)
	{
		if (reservedRam + ram > maxRam) reservedRam = maxRam;
		else if (reservedRam + ram < 0) reservedRam = 0;
		else reservedRam += ram;
	}

	/**
	 * Returns the maximum RAM capacity of the Invoker.
	 *
	 * @return The maximum RAM capacity.
	 */
	@Override
	public long	getMaxRam() {
		return (maxRam);
	}

	/**
	 * Returns the amount of unused RAM on the Invoker.
	 *
	 * @return The unused RAM.
	 */
	public long getUnusedRam() {
		return maxRam - usedRam;
	}

	/**
	 * Selects the Invoker to execute a function based on the required RAM.
	 *
	 * @param ram The required RAM for the function execution.
	 * @return The selected Invoker.
	 * @throws NoPolicyManagerRegistered It will never be thrown.
	 * @throws NoInvokerAvailable       If there is no Invoker with enough max RAM to execute the Invokable.
	 * @throws RemoteException          If an exception occurs during RMI communication.
	 */
	@Override
	public InvokerInterface selectInvoker(long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException {
		if (this.maxRam < ram) throw new NoInvokerAvailable("Not enough ram to assign this invoker.");
		return this;
	}

	/**
	 * Executes a synchronous function passed by reference, applying observers and decorators.
	 *
	 * @param invokable The Invokable function to be executed along with its RAM requirement.
	 * @param args      The arguments needed by the function.
	 * @param id        The identifier of the function.
	 * @param <T>       The type of input argument.
	 * @param <R>       The type of the result.
	 * @return The result of the function invocation.
	 * @throws Exception If an exception occurs during execution.
	 */
	public <T, R> R invoke(Invokable<T,R> invokable, T args, String id) throws Exception {
		MetricRecollector metricsRecollector = new MetricRecollector(id, this);
		metricsRecollector.initializeObservers();

		this.reserveRam(invokable.getRam());
		usedRam += invokable.getRam();

		metricsRecollector.executeObservers();

		R result = ((Function<T, R>)invokable.retrieveInvokable()).apply(args);

		usedRam -= invokable.getRam();
		this.reserveRam(-invokable.getRam());
		
		metricsRecollector.notifyObservers();

		return result;
	}

	/**
	 * Attempts to execute a function asynchronously.
	 * If there is no available RAM, it will wait until it gets a notification that it can be executed.
	 * If the pool of threads is full, it will also wait.
	 *
	 * @param invokable The Invokable function to be executed asynchronously along with its RAM requirement.
	 * @param args      The arguments needed by the function.
	 * @param id        The identifier of the function.
	 * @param <T>       The type of input argument.
	 * @param <R>       The type of the result.
	 * @return A Future representing the asynchronous result of the function invocation.
	 * @throws Exception If an exception occurs during execution.
	 */
	public <T, R> Future<R> invokeAsync(Invokable<T,R> invokable, T args, String id) throws Exception {
		MetricRecollector metricsRecollector = new MetricRecollector(id, this);
		metricsRecollector.initializeObservers();
		Function<T, R> function = (Function<T, R>)invokable.retrieveInvokable();

		this.reserveRam(invokable.getRam());

		Future<R> futureResult = executor.submit( 
			() -> {
				R result;

				synchronized (this) {
					while (getUnusedRam() - invokable.getRam() < 0)
					{
						try {
							wait();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					usedRam += invokable.getRam();
					metricsRecollector.executeObservers();
				}
				result = function.apply(args);
				synchronized (this) {
					usedRam -= invokable.getRam();
					this.reserveRam(-invokable.getRam());
					metricsRecollector.notifyObservers();
					notify();
				}
				return result;
			}
		);
		return futureResult;
	}

	/**
	 * Shuts down the executor of the Invoker. Must be called when the application finishes.
	 */
	@Override
	public void shutdownInvoker() {
		executor.shutdown();
	}

	/**
	 * Does nothing. Needed to implement because the contract specifies it.
	 */
	@Override
	public void	setPolicyManager(PolicyManager policyManager) throws RemoteException {
	}

	/**
	 * Does nothing. Needed to implement because the contract specifies it.
	 */
	@Override
	public void setDistributionPolicyManager(int size, long ram) throws RemoteException, NoInvokerAvailable {
	}

	/**
	 * Does nothing. Needed to implement because the contract specifies it.
	 */
	@Override
	public void registerInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException {
	}

	/**
	 * Does nothing. Needed to implement because the contract specifies it.
	 */
	@Override
	public void deleteInvoker(InvokerInterface invoker) throws OperationNotValid, RemoteException {
	}
	
}
