package dynamic_proxy.proxies;

import java.util.Map;

public interface CalculatorProxy {

	public Integer suma(Map<String, Integer> map);
	public Integer resta(Map<String, Integer> map);

}
