package services.countwords;

import java.util.List;

import core.application.Action;

//reduce of CountWordsAction
public class CountWordsReduce implements Action<List<Long>, Long>
{
	@Override
	public Long apply(List<Long> arg)
	{
		Long	count;

		count = 0L;
		for (Long number : arg) {
			count += number;
		}
		return (count);
	}
}
