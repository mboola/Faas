package scala

import core.application.Controller;
import core.invoker.Invoker;
import policymanager.PolicyManager;
import policymanager.RoundRobin;

object Main {
	
	def expensiveComputation(x: Int): String = {
		println(s"Performing expensive computation for $x")
		(x * x).toString
	}

	def main(args: Array[String]): Unit = {
		val controller = Controller.instantiate()
		val policyManager = new RoundRobin()
		controller.setPolicyManager(policyManager)
		val invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker)
		controller.registerAction("computation", expensiveComputation, 1)
		controller.invoke("computation", 5)
	}
}

