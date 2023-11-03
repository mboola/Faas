package application;
import java.util.List;

public interface PolicyManager {

	public Invoker getInvoker(List<Invoker> invokers, int ram) throws NoInvokerAvaiable;

}
