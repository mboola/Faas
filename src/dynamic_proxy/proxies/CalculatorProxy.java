package dynamic_proxy.proxies;

import java.util.Map;

public interface CalculatorProxy {

	public int suma(Map<String, Integer> map);
	public int resta(Map<String, Integer> map);

}
