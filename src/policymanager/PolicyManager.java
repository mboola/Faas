package policymanager;

import java.rmi.RemoteException;
import java.util.List;

import core.exceptions.NoInvokerAvailable;
import core.exceptions.NoPolicyManagerRegistered;
import core.invoker.InvokerInterface;

public interface PolicyManager {

	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws NoPolicyManagerRegistered, NoInvokerAvailable, RemoteException;

	public PolicyManager	copy();
}
