package services.otheractions;

import core.application.Action;

public class FactorialAction implements Action<Long, Long> {

	@Override
	public Long apply(Long number) {
		if (number <= 0) return ( (long) 0 );
		if (number == 1) return ( (long) 1 );
		return ( (long) number * apply(number - 1));
	}

}