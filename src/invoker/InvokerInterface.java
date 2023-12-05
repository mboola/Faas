package invoker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.Future;

import application.Action;
import application.Invokable;

public interface InvokerInterface extends Remote {

    public String getId() throws RemoteException;
    
	/**
	 * Getter of the ram being used.
	 * 
	 * @return Ram being used.
	 */
	public long	getRamUsed() throws RemoteException;

	/**
	 * Getter of the ram avaiable to use.
	 * 
	 * @return Ram avaiable to use.
	 */
	public long	getAvailableRam() throws RemoteException;
	
	/**
	 * Getter of the max ram of the Invoker.
	 * 
	 * @return Max ram to be used by this invoker.
	 */
	public long	getMaxRam() throws RemoteException;

	/**
	 * This method is called to execute a sync function passed by reference, applying all the observers and decorators.
	 * 
	 * @param action Function to be executed and the ram it consumes.
	 * @param args Arguments needed by the function, of type T
	 * @param id Identifier of the function. Needed by the observers and decorators to store data correctly.
	 * @return The result of the function invoked, of type R
	 * @throws Exception //TODO: i dont remember
	 */
	public <T, R> R invoke(Invokable invokable, T args, String id) throws Exception;

	//TODO: javadoc this
	// This function tries to execute the function passed by parameter.
	// If there is no space in the pool, it waits and then it gets invoked.
	public <T, R> Future<R> invokeAsync(Invokable invokable, T args, String id) throws Exception;

	/**
	 * This shuts down the executor of the Invoker. Must be called when the application finishes.
	 */
	public void shutdownInvoker() throws RemoteException;
}
