package services.proxies;

import java.util.Map;

public class Calculator implements CalculatorProxy{

	public Calculator() {
	}

	public Object suma(Map<String, Integer> map) {
		return (map.get("x") + map.get("y"));
	}

	public Object resta(Map<String, Integer> map) {
		return (map.get("x") - map.get("y"));
	}

}
