package dynamic_proxy.proxies;

import java.util.Map;
import java.util.concurrent.Future;

public interface CalculatorProxy {

	public Future<Integer> suma(Map<String, Integer> map);
	public Future<Integer> resta(Map<String, Integer> map);

}
