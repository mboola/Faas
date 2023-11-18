package dynamic_proxy.proxies;

import java.util.Map;
import java.util.concurrent.Future;

public class Calculator implements CalculatorProxy{

	public Calculator() {
	}

	public Future<Integer> suma(Map<String, Integer> map) {
		return (null);
	}

	public Future<Integer> resta(Map<String, Integer> map) {
		return (null);
	}

}
