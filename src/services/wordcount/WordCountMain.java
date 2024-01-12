package services.wordcount;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import core.application.Controller;
import core.dynamicproxy.DynamicProxy;
import core.invoker.Invoker;
import policymanager.RoundRobin;

@SuppressWarnings({"unchecked"})
public class WordCountMain {

	public static void main(String[] args) 
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(200, 4);

		Function<Object, Object> countWords = 
			(obj) -> {
				return (new WordCount());
			}
		;

		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("WordCountService", countWords, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try {
			WordCountProxy countWordsProxy = (WordCountProxy)DynamicProxy.getActionProxy("WordCountService", true);
			List<String> lines = (List<String>)countWordsProxy.mapFile("src\\services\\wordcount\\test.txt");

			List<Map<String, Long>> words = new LinkedList<>();
			for (String string : lines) {
				words.add((Map<String, Long>)countWordsProxy.wordCount(string));
			}

			Object result = countWordsProxy.wordCountReduce(words);
			System.out.println("Number of words is: " + result);
			controller.shutdownAllInvokers();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
