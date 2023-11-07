package dynamic_proxy;

import java.util.List;
import java.util.concurrent.Future;

import application.Invoker;
import policy_manager.PolicyManager;

public interface ActionProxy {

	public void registerAction(String id, Object f, int ram);
	public void removeAction(String id);
	public void	listActions();

	public <T, R> R invoke(String id, T args) throws Exception;
	public <T, R> List<R> invoke(String id, List<T> args) throws Exception;
	public <T, R> Future<R> invoke_async(String id, T args) throws Exception;

	public void	addPolicyManager(PolicyManager policyManager);

	public void registerInvoker(Invoker invoker);
	public void deleteInvoker(Invoker invoker);
	public void shutdownAllInvokers();

	public boolean hasMapAction(String id);

}
