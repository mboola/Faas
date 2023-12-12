package action;

import java.util.List;


//reduce of CountWordsAction
public class AddListAction implements Action<List<Long>, Long>
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
