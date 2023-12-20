package services.wordcount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCount implements WordCountProxy{

	@Override
	public Object mapFile(String pathName) throws IOException
	{
		List<String> lines;
		Path filePath = Paths.get(pathName);
		lines = Files.readAllLines(filePath);
		return (lines);
	}

	@Override
	public Object wordCount(String allWords) {
		Map<String, Long>	mapToReturn;
		String[]			words;
		Long				count;

		mapToReturn = new HashMap<String, Long>();
		words = allWords.split(" ");
		
		for (String word : words) {
			if (mapToReturn.containsKey(word))
			{
				count = mapToReturn.get(word);
				count++;
				mapToReturn.put(word, count);
			}
			else
				mapToReturn.put(word, 1L);
		}
		return (mapToReturn);
	}

	@Override
	public Object wordCountReduce(List<Map<String, Long>> words)
	{
		Map<String, Long> 	mapToReturn;
		String 				key;
		Long				value, mapValue;

		mapToReturn = new HashMap<String, Long>();
		for (Map<String,Long> map : words)
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
