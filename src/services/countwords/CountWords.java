package services.countwords;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CountWords implements CountWordsProxy{
	
	public Object mapFile(String pathName) throws IOException
	{
		List<String> lines;
		Path filePath = Paths.get(pathName);
		lines = Files.readAllLines(filePath);
		return (lines);
	}

	public Object countWords(String allWords)
	{
		String[]	words;

		words = allWords.split(" ");
		return (long) (words.length);
	}

	public Object countWordsReduce(List<Long> arg)
	{
		Long	count;

		count = 0L;
		for (Long number : arg) {
			count += number;
		}
		return (count);
	}

}
