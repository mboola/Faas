package policymanager;

import java.util.List;

import core.invoker.InvokerInterface;

public interface PolicyManager {

	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws Exception;

	public PolicyManager	copy();
}
