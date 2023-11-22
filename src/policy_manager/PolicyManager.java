package policy_manager;
import java.util.List;

import RMI.InvokerInterface;

public interface PolicyManager {

	public InvokerInterface getInvoker(List<InvokerInterface> invokers, int ram) throws Exception;

}
