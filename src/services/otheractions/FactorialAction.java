package services.otheractions;

import core.application.Action;

public class FactorialAction implements Action<Long, Long> {

	private Long factorial(Long number) {
		return ( (long) number * factorial(number - 1));
	}

	@Override
	public Long apply(Long number) {
		if (number <= 0) return ( (long) 0 );
		if (number == 1) return ( (long) 1 );
		return factorial(number);
	}

}