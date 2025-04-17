package services.countwords;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import core.application.Action;
import core.dynamicproxy.DynamicProxy;

public class CountWordsAction implements Action<String, Object> {

	//This returns a Long
	@Override
	public Object apply(String arg) {
		try {
			List<Long> result = new LinkedList<>();

			CountWordsProxy countWordsProxy = (CountWordsProxy)DynamicProxy.getActionProxy("CountWordsService", false);

			Object text = countWordsProxy.mapFile(arg);

			List<String> finalText = ((Future<List<String>>)text).get();
			List<Object> wordCount = new LinkedList<>();

			for (String string : finalText) {
				wordCount.add(countWordsProxy.countWords(string));
			}
			for (Object future : wordCount) {
				result.add(((Future<Long>)future).get());
			}
			Object words = countWordsProxy.countWordsReduce(result);
			return ((Future<Long>)words).get();
		}
		catch (Exception e) {
			return null;
		}
	}

}
