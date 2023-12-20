package services.countwords;

import java.io.IOException;
import java.util.List;

public interface CountWordsProxy {

	public Object mapFile(String pathName) throws IOException;

	public Object countWords(String allWords);

	public Object countWordsReduce(List<Long> arg);

}
