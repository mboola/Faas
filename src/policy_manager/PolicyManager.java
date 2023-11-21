package policy_manager;
import java.util.List;

import application.Invoker;
import faas_exceptions.NoInvokerAvailable;

public interface PolicyManager {

	public Invoker getInvoker(List<Invoker> invokers, int ram) throws NoInvokerAvailable;

}
