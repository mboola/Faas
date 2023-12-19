package invoker;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import application.Invokable;
import application.Metric;
import faas_exceptions.NoInvokerAvailable;
import observer.Observer;
import policy_manager.PolicyManager;

public class Invoker implements InvokerInterface, Serializable {

	//all good here
	private static final int 		MAX_THREADS = 8; //TODO: talk about this 
	//TODO: change this to dynamically reserved or parameter
	private static long				numInvokers = 0;

	private final String	id;
	private final long		maxRam;			//lo maximo que podemos usar para ejecutar funciones: ej si tenemos 5 y queremos ejecutar algo de 6 no se podra //TODO; wtf
	
	private long			reservedRam;	//la que se va a usar pero aun no. Usada desde el PolicyManager
	private long			usedRam;		//ram en uno actualmente en los invokers

	public long getAvailableRam()
	{
		return (maxRam - reservedRam);
	}

	public void reserveRam(long ram) throws Exception
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


	private transient ExecutorService 		executor;

	private static List<Observer>								observers = new LinkedList<Observer>();
	private static Function<Invokable, Function<Object, Object>>	decoratorInitializer = null;
	
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

	/**
	 * Adds an observer to the list of observers to be used by all invokers.
	 * 
	 * @param observer Observer that will be notified when a function is invoked.
	 */
	public static void addObserver(Observer observer)
	{
		//TODO: check if the observer is already added
		Invoker.observers.add(observer);
	}

	/**
	 * Removes an observer to the list of observers to be used by all invokers.
	 * 
	 * @param observer Observer that won't be notified when a function is invoked.
	 */
	public static void removeObserver(Observer observer)
	{
		//TODO: check of the observer is on the list
		Invoker.observers.remove(observer);
	}

	
	public static void setDecoratorInitializer(Function<Invokable, Function<Object, Object>> decoratorInitializer)
	{
		Invoker.decoratorInitializer = decoratorInitializer;
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
	public InvokerInterface selectInvoker(long ram) throws Exception
	{
		if (this.maxRam < ram) throw new NoInvokerAvailable("Not enough ram to assign this invoker.");
		return (this);
	}

	public void	setPolicyManager(PolicyManager policyManager)
	{
	}

	//TODO: javadocs
	//TODO specify when I wanna use this Decorators. Not always are needed. Specially in testing
	@SuppressWarnings({"unchecked"})
	private <T, R> Function<T, R> applyDecorators(Invokable invokable)
	{
		if (Invoker.decoratorInitializer == null)
			return ((Function<T, R>)invokable.getInvokable());
		return ((Function<T, R>)Invoker.decoratorInitializer.apply(invokable));
	}

	private void initializeObservers(String id) throws Exception 
	{
		for (Observer observer : observers) {
			observer.initialize(id, this);
		}
	}

	/**
	 * This initializes the value of all the observers being used in the invocation.
	 * 
	 * @param id Id of the function. Needed by the observers to update the content of a dictionary in the controller.
	 * @return Map of metrics initialized. These metrics will be modified by 'notifyAllObservers' to create final metrics.
	 * @throws Exception
	 */
	private synchronized HashMap<Observer, Metric<Object>> executeObservers(String id) throws Exception 
	{
		HashMap<Observer, Metric<Object>> metrics = new HashMap<Observer, Metric<Object>>();

		for (Observer observer : observers) {
			metrics.put(observer, observer.execution(id, this));
		}
		return (metrics);
	}

	/**
	 * This modifies all the values of the metrics created by 'initializeAllObservers'
	 * 
	 * @param metrics Map of all the metrics to be updated by the observers
	 * 
	 * <p><strong>Note:</strong> If the list of observers changed between initialization and this method,
	 * two things can happen:
	 * <ul>
	 * 		<li> If observers were added, the new observers will not be notified.</li>
	 * 		<li> If observers were removed, the removed observers will not be notified.</li>
	 * </ul>
	 * This is to ensure a correct funcionality of the observers.
	 * </p>
	 */
	private void notifyObservers(HashMap<Observer, Metric<Object>> metrics)
	{
		Metric<Object>	metric;

		for (Observer observer : observers) {
			metric = metrics.get(observer);
			if (metric != null)
				observer.update(metric);
		}
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
	public <T, R> R invoke(Invokable invokable, T args, String id) throws Exception
	{
		Function<T, R>						functionDecorated;
		R									result;
		HashMap<Observer, Metric<Object>>	metricsList;

		initializeObservers(id);
		metricsList = executeObservers(id);

		result = ((Function<T, R>)invokable.getInvokable()).apply(args);

		notifyObservers(metricsList);

		return (result);
	}

	//TODO: javadoc this
	// This function tries to execute the function passed by parameter.
	// If there is no space in the pool, it waits and then it gets invoked.
	public <T, R> Future<R> invokeAsync(Invokable invokable, T args, String id) throws Exception
	{
		Function<T, R>	functionDecorated;
		Future<R>		futureResult;

		initializeObservers(id);
		functionDecorated = applyDecorators(invokable);

		this.reserveRam(invokable.getRam());

		futureResult = executor.submit( 
			() -> {
				R	result;
				HashMap<Observer, Metric<Object>>	metricsList;

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
					metricsList = executeObservers(id);
				}
				result = functionDecorated.apply(args);
				synchronized (this)
				{
					usedRam -= invokable.getRam();
					this.reserveRam(-invokable.getRam());
					notifyObservers(metricsList);
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
