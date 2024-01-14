package services.countwords;

import java.util.concurrent.Future;
import java.util.function.Function;

import core.application.Action;
import core.application.Controller;
import core.invoker.Invoker;
import core.metrics.MetricCollection;
import decorator.TimerDecorator;
import observer.TimerObserver;
//import policymanager.BigGroup;
//import policymanager.GreedyGroup;
import policymanager.RoundRobin;
//import policymanager.UniformGroup;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UltimateCountWordsMain {

	public static void main(String[] args) 
	{
		Controller controller = Controller.instantiate();
		Invoker invoker = Invoker.createInvoker(10, 4);
		Invoker invoker1 = Invoker.createInvoker(10, 4);
		Invoker invoker2 = Invoker.createInvoker(10, 4);

		Function<Object, Object> countWords = 
			(obj) -> {
				return (new CountWords());
			}
		;
		TimerObserver timerObserver = new TimerObserver();
		try {
			MetricCollection.instantiate().addObserver(timerObserver);
			controller.registerInvoker(invoker);
			controller.registerInvoker(invoker1);
			controller.registerInvoker(invoker2);
			controller.setPolicyManager(new RoundRobin());
			controller.registerAction("CountWordsService", countWords, 1);

			Action ultimateCountWords = new CountWordsAction();
			TimerDecorator time = new TimerDecorator<>(ultimateCountWords);
			controller.registerAction("ultimateCountWords", time, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Future<Long> result = controller.invoke_async("ultimateCountWords", "src\\services\\countwords\\test.txt");
			System.out.println(result.get());
			result = controller.invoke_async("ultimateCountWords", "src\\services\\countwords\\test.txt");
			System.out.println(result.get());
			result = controller.invoke_async("ultimateCountWords", "src\\services\\countwords\\test.txt");
			System.out.println(result.get());
			result = controller.invoke_async("ultimateCountWords", "src\\services\\countwords\\test.txt");
			System.out.println(result.get());
			result = controller.invoke_async("ultimateCountWords", "src\\services\\countwords\\test.txt");
			System.out.println(result.get());
			controller.shutdownAllInvokers();
			System.out.println("Total   time: " + timerObserver.calculateAllTime("ultimateCountWords") + "ns.");
			System.out.println("Minimum time: " + timerObserver.calculateMinTime("ultimateCountWords") + "ns.");
			System.out.println("Maximum time: " + timerObserver.calculateMaxTime("ultimateCountWords") + "ns.");
			System.out.println("Average time: " + timerObserver.calculateAverageTime("ultimateCountWords") + "ns.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
