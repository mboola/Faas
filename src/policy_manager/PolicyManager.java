package policy_manager;
import java.util.List;

import application.Invoker;
import exceptions.NoInvokerAvaiable;

public interface PolicyManager {

	public Invoker getInvoker(List<Invoker> invokers, int ram) throws NoInvokerAvaiable;

}
