package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import action.Action;
import action.AddListAction;
import action.CountWordsAction;
import action.MapFileAction;
import invoker.Invoker;
import invoker.CompositeInvoker;
import observer.InvocationObserver;
import policy_manager.RoundRobin;

public class Faas {
	public static void main(String[] args) 
	{
		Controller controller = Controller.instantiate();
		Invoker.setController(controller);
		CompositeInvoker invokerComposite = CompositeInvoker.createInvoker(1);
		Invoker.addObserver(new InvocationObserver());
		Action mapAction = new MapFileAction();
		Action countWordsAction = new CountWordsAction();
		Action addListAction = new AddListAction();
		Invoker invokerSimple = Invoker.createInvoker(1);
		Invoker invokerSimple1 = Invoker.createInvoker(1);
		Invoker invokerSimple2 = Invoker.createInvoker(1);
		try
		{
			controller.setPolicyManager(new RoundRobin());
			controller.registerInvoker(invokerComposite);
			controller.registerAction("MapAction", mapAction, 1);
			controller.registerAction("CountWordsAction", countWordsAction, 1);
			controller.registerAction("AddListAction", addListAction, 1);
			controller.registerInvoker(invokerSimple);
			invokerComposite.registerInvoker(invokerSimple1);
			invokerComposite.registerInvoker(invokerSimple2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		String	fileInput = "aa bb c de eferlmkerflk rreklmf emrklwf mklermlkf wremklfmkler kmlwerfkml kmelrf mker";

		//here get string with words from file or something
		List<String>		chunksOfWords;
		List<Future<Long>>	words;
		List<Long>			result;
		List<List<Long>>	resultList;

		words = new LinkedList<Future<Long>>();
		result = new LinkedList<Long>();
		resultList = new LinkedList<List<Long>>();
		try {
			chunksOfWords = controller.invoke("MapAction", fileInput);
			for (String str : chunksOfWords) {
				words.add(controller.invoke_async("CountWordsAction", str));
			}
			for (Future<Long> future : words) {
				result.add(future.get());
			}
			resultList.add(result);
			result = controller.invoke("AddListAction", resultList);
			System.out.println(result.get(0));	//result
		}
		catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
		
		try {
			controller.shutdownAllInvokers();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}