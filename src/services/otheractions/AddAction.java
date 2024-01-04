package services.otheractions;

import java.util.Map;

import core.application.Action;

public class AddAction implements Action<Map<String, Integer>, Integer> {

	@Override
	public Integer apply(Map<String, Integer> arg) {
		return (arg.get("x") + arg.get("y"));
	}

}
