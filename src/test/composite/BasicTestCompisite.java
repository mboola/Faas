package test.composite;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import application.Controller;
import faas_exceptions.OperationNotValid;
import invoker.Invoker;
import invoker.InvokerComposite;
import observer.IdObserver;
import policy_manager.PolicyManager;
import policy_manager.RoundRobin;

public class BasicTestCompisite {

	private Controller			controller;
	private InvokerComposite	invoker;

	/*
	 * This gets called before each Test. 
	 * We instantiate a controller with basic components so it can work.
	 * (for some reason BeforeEach couldn't be used)
	 */
	@Before
	public void	controllerInitialization()
	{
		controller = Controller.instantiate();
		PolicyManager policyManager = new RoundRobin();
		controller.addPolicyManager(policyManager);
		Invoker.setController(controller);
		invoker = InvokerComposite.createInvoker(1);
		Invoker.addObserver(new IdObserver());
		try {
			controller.registerInvoker(invoker);
		} catch (OperationNotValid e) {
			assertTrue(false);
		}
	}

	/**
	 * Having one invoker composite(A) with two invokers inside(B & C) and another invoker (D), 
	 * when we execute 4 functions the execution path should be B, D, C, D
	 * A will only be used when childs are full and A has space
	 */
	@Test
	public void	testCompositeInvokerSyncInvokation()
	{
		Invoker invokerSimple1 = Invoker.createInvoker(2);
		Invoker invokerSimple2 = Invoker.createInvoker(3);
		Function<Map<String, Integer>, Integer> f = x -> x.get("x") - x.get("y");
		try
		{
			invoker.registerInvoker(invokerSimple1);
			invoker.registerInvoker(invokerSimple2);
			controller.registerAction("sub", f, 1);
			Integer result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			//get invoker used
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			//get invoker used
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			//get invoker used
			result = (Integer) controller.invoke("sub", Map.of("x", 2, "y", 1));
			String str = controller.metrics.getData("IdController", "sub");
			assertEquals(str, "1");
		}
		catch (Exception e){
			assertTrue(false);
		}

	}

}
