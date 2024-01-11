package core.invoker;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import core.application.Invokable;
import core.exceptions.*;
import core.metrics.MetricRecollector;
import policymanager.PolicyManager;

public class Invoker implements InvokerInterface, Serializable {

	//all good here
	private static final int 		MAX_THREADS = 8; //TODO: talk about this 
	//TODO: change this to dynamically reserved or parameter
	private static long				numInvokers = 0;

	private final String	id;
	private final long		maxRam;			//lo maximo que podemos usar para ejecutar funciones: ej si tenemos 5 y queremos ejecutar algo de 6 no se podra //TODO; wtf
	
	private long			reservedRam;	//la que se va a usar pero aun no. Usada desde el PolicyManager
	private long			usedRam;		//ram en uno actualmente en los invokers

	private transient ExecutorService 	executor;

	public long getAvailableRam()
	{
		return (maxRam - reservedRam);
	}

	public void reserveRam(long ram)
	{
		if (reservedRam + ram > maxRam) reservedRam = maxRam;
		else if (reservedRam + ram < 0) reservedRam = 0;
		else reservedRam += ram;
	}

	//invoker
	public long getUnusedRam()
	{
		return (maxRam - usedRam);
	}

	/**
	 * Getter of the ram being used.
	 * 
	 * @return Ram being used.
	 */
	public long	getUsedRam()
	{
		return (usedRam);
	}

	public long	getReservedRam()
	{
		return (reservedRam);
	}
	
	/**
	 * Getter of the max ram of the Invoker.
	 * 
	 * @return Max ram to be used by this invoker.
	 */
	public long	getMaxRam()
	{
		return (maxRam);
	}
	
	/**
	 * This method creates an Invoker.
	 * 
	 * @param ram The max ram of the Invoker.
	 * @return The Invoker created, or null if ram is lesser or equal to zero.
	 */
	public static Invoker createInvoker(long ram)
	{
		if (ram <= 0)
			return (null);
		else
			return (new Invoker(ram));
	}

	/**
	 * Constructs a new instance of Invoker.
	 * 
	 * @param ram The max ram of the Invoker.
	 * 
	 * <p><strong>Note:</strong> Constuctor will only be called from 'createInvoker' to ensure no Invokers with invalid parameters are created.</p>
	 */
	protected Invoker(long ram)
	{
		this.maxRam = ram;
		usedRam = 0;
		reservedRam = 0;

		executor = Executors.newFixedThreadPool(MAX_THREADS);

		id = ((Long)numInvokers).toString();
		numInvokers++;
	}

	public String getId()
	{
		return (id);
	}

	/**
	 * This selects an invoker to invoke a function.
	 */
	public Invoker getInvoker()
	{
		return (this);
	}

	/**
	 * Method used to select a invoker to execute a function based on the ram it consumes and the policy we have assigned.
	 * @param ram
	 * @return
	 * @throws Exception <p>The exception can be caused because:</p>
	 * <ul>
	 * 	<li>NoPolicyManagerRegistered: There is no policyManager registered.</li>
	 * 	<li>NoInvokerAvailable: There is no invoker with enough max ram to execute the invokable.</li>
	 *  <li>Exeption: something goes wrong with RMI.</li>
	 * </ul>
	 */
	@Override
	public InvokerInterface selectInvoker(long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException
	{
		if (this.maxRam < ram) throw new NoInvokerAvailable("Not enough ram to assign this invoker.");
		return (this);
	}

	public void	setPolicyManager(PolicyManager policyManager) throws RemoteException
	{
	}

	public void setDistributionPolicyManager(int size, long ram)
			throws NoInvokerAvailable, RemoteException
	{
	}

	/**
	 * This method is called to execute a sync function passed by reference, applying all the observers and decorators.
	 * 
	 * @param action Function to be executed and the ram it consumes.
	 * @param args Arguments needed by the function, of type T
	 * @param id Identifier of the function. Needed by the observers and decorators to store data correctly.
	 * @return The result of the function invoked, of type R
	 * @throws Exception //TODO: i dont remember
	 */
	public <T, R> R invoke(Invokable<T,R> invokable, T args, String id) throws Exception
	{
		MetricRecollector	metricsRecollector;
		R									result;

		metricsRecollector = new MetricRecollector(id, this);
		metricsRecollector.initializeObservers();

		this.reserveRam(invokable.getRam());
		usedRam += invokable.getRam();

		metricsRecollector.executeObservers();

		result = ((Function<T, R>)invokable.retrieveInvokable()).apply(args);

		usedRam -= invokable.getRam();
		this.reserveRam(-invokable.getRam());
		
		metricsRecollector.notifyObservers();

		return (result);
	}

	//TODO: javadoc this
	// This function tries to execute the function passed by parameter.
	// If there is no space in the pool, it waits and then it gets invoked.
	public <T, R> Future<R> invokeAsync(Invokable<T,R> invokable, T args, String id) throws Exception
	{
		Function<T, R>	function;
		MetricRecollector	metricsRecollector;
		Future<R>		futureResult;

		metricsRecollector = new MetricRecollector(id, this);
		metricsRecollector.initializeObservers();
		function = (Function<T, R>)invokable.retrieveInvokable();

		this.reserveRam(invokable.getRam());

		futureResult = executor.submit( 
			() -> {
				R	result;

				synchronized (this)
				{
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
				synchronized (this)
				{
					usedRam -= invokable.getRam();
					this.reserveRam(-invokable.getRam());
					metricsRecollector.notifyObservers();
					notify();
				}
				return (result);
			}
		);
		return (futureResult);
	}

	/**
	 * This shuts down the executor of the Invoker. Must be called when the application finishes.
	 */
	public void shutdownInvoker()
	{
		executor.shutdown();
	}

	@Override
	public void registerInvoker(InvokerInterface invoker) throws Exception {
	}

	@Override
	public void deleteInvoker(InvokerInterface invoker) throws Exception {
	}
}
