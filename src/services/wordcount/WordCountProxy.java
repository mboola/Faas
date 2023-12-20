package services.wordcount;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface WordCountProxy {

	public Object mapFile(String pathName) throws IOException;

	//this returns a Map<String, Long>
	public Object wordCount(String allWords);

	//this returns a Map<String, Long>
	public Object wordCountReduce(List<Map<String, Long>> words);

}
