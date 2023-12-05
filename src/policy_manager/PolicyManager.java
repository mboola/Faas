package policy_manager;
import java.util.List;

import invoker.InvokerInterface;

public interface PolicyManager {

	public InvokerInterface getInvoker(List<InvokerInterface> invokers, long ram) throws Exception;

}
