package action;

import java.util.HashMap;
import java.util.Map;

public class WordCountAction implements Action<String, Map<String, Long>> {

	@Override
	public Map<String, Long> apply(String arg)
	{
		Map<String, Long>	mapToReturn;
		String[]			words;
		Long				count;

		mapToReturn = new HashMap<String, Long>();
		words = arg.split(" ");
		
		for (String word : words) {
			if (mapToReturn.containsKey(word))
			{
				count = mapToReturn.get(word);
				count++;
				mapToReturn.put(arg, count);
			}
			else
				mapToReturn.put(arg, 1L);
		}
		return (mapToReturn);
	}

}
