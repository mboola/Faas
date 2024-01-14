package services.countwords;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import core.application.Action;
import core.application.Controller;
import core.dynamicproxy.DynamicProxy;
import core.invoker.Invoker;
import decorator.TimerDecorator;
import policymanager.BigGroup;
import policymanager.GreedyGroup;
import policymanager.RoundRobin;

@SuppressWarnings({"unchecked"})
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

		try {
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
			controller.shutdownAllInvokers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
