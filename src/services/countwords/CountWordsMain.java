package services.countwords;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import core.application.Controller;
import core.dynamicproxy.DynamicProxy;
import core.invoker.Invoker;
import policymanager.RoundRobin;

public class CountWordsMain {

	public static void main(String[] args) 
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(200);

		Function<Object, Object> countWords = 
			(obj) -> {
				return (new CountWords());
			}
		;

		try {
			controller.registerInvoker(invoker);
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("CountWordsService", countWords, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		List<Long> result = new LinkedList<>();

		try {
			CountWordsProxy countWordsProxy = (CountWordsProxy)DynamicProxy.getActionProxy("CountWordsService");
			Object text = countWordsProxy.mapFile("src\\services\\countwords\\test.txt");

			List<String> finalText = ((Future<List<String>>)text).get();
			List<Object> wordCount = new LinkedList<>();

			for (String string : finalText) {
				wordCount.add(countWordsProxy.countWords(string));
			}
			for (Object future : wordCount) {
				result.add(((Future<Long>)future).get());
			}
			Object words = countWordsProxy.countWordsReduce(result);
			Long	finalResult = ((Future<Long>)words).get();
			System.out.println("Number of words is: " + finalResult);
			controller.shutdownAllInvokers();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
