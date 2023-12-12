package action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//reduce of WordCountAction
public class AddMapAction implements Action<List<Map<String, Long>>, Map<String, Long>>
{

	@Override
	public Map<String, Long> apply(List<Map<String, Long>> arg)
	{
		Map<String, Long> 	mapToReturn;
		String 				key;
		Long				value, mapValue;

		mapToReturn = new HashMap<String, Long>();
		for (Map<String,Long> map : arg)
		{
			//here get all values and keys and add them to mapToReturn
			for (Map.Entry<String,Long> entry : map.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				if (mapToReturn.containsKey(key))
				{
					mapValue = mapToReturn.get(key);
					mapValue += value;
					mapToReturn.put(key, mapValue);
				}
				else
					mapToReturn.put(key, value);
			}
		}
		return (mapToReturn);
	}

}
