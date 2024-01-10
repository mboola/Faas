package scala

import java.util.function.{Function => JFunction}

import core.application.Controller;
import core.invoker.Invoker;
import core.dynamicproxy.DynamicProxy;
import policymanager.PolicyManager;
import policymanager.RoundRobin;
import core.application.Action;
import services.proxies.Calculator;
import services.proxies.CalculatorProxy;

object Main {
	
	def expensiveComputation(x: Int): String = {
		println(s"Performing expensive computation for $x")
		(x * x).toString
	}

	trait Decorator[T, R] extends JFunction[T, R]

	// Concrete implementation of the interface that adds caching behavior
	class CacheDecorator[T, R](function: JFunction[T, R]) extends Decorator[T, R] {
		private val cache = collection.mutable.Map[T, R]()
		override def apply(arg: T): R = cache.getOrElseUpdate(arg, function(arg))
	}

	class TimerDecorator[T, R](function: JFunction[T, R]) extends Decorator[T, R] {
		override def apply(arg: T): R = {
			val startTime = System.nanoTime()
			val result = function.apply(arg)
			val endTime = System.nanoTime()
			println(s"Time of execution is ${endTime - startTime} ns.")
			result
		}
	}

	class WordCountAction extends Action[String, Map[String, Int]] {
		override def apply(text: String): Map[String, Int] = {
			val words = text.toLowerCase.split("\\W+").filter(_.nonEmpty)	//aqui el \\W+ místico lo que hace es separar por carácteres que NO sean palabras.
			words.groupBy(identity).view.mapValues(_.length).toMap			//el filter se le pasa una funcion sin todos los parámetros, que básicamente serán todas las palabras y nos guaradará
		}
	}

	def wordCount(text: String): Map[String, Int] = {
		val words = text.toLowerCase.split("\\W+").filter(_.nonEmpty)	//aqui el \\W+ místico lo que hace es separar por carácteres que NO sean palabras.
		words.groupBy(identity).view.mapValues(_.length).toMap			//el filter se le pasa una funcion sin todos los parámetros, que básicamente serán todas las palabras y nos guaradará
	}																	//las que no esten vacias

	def countWords(text: String): Map[String, Int] = {
		val words = text.split("\\s+")
		Map("total" -> words.length)
	}


	def main(args: Array[String]): Unit = {

		//configure Controller
		val controller = Controller.instantiate()
		val policyManager = new RoundRobin()
		controller.setPolicyManager(policyManager)
		val invoker = Invoker.createInvoker(1);
		controller.registerInvoker(invoker)

		controller.registerAction("computation", expensiveComputation, 1)

		val timer1DecoratedFunction = new TimerDecorator(expensiveComputation)
		controller.registerAction("computationTimer", timer1DecoratedFunction, 1)

		val cacheDecoratedFunction = new CacheDecorator(expensiveComputation)
		val both1DecoratedFunction = new TimerDecorator(cacheDecoratedFunction)
		controller.registerAction("computationBoth1", both1DecoratedFunction, 1)

		val timer2DecoratedFunction = new TimerDecorator(expensiveComputation)
		val both2DecoratedFunction = new CacheDecorator(timer2DecoratedFunction)
		controller.registerAction("computationBoth2", both2DecoratedFunction, 1)

		controller.invoke("computation", 5)
		controller.invoke("computationTimer", 5)
		controller.invoke("computationBoth1", 5)
		controller.invoke("computationBoth1", 5)
		controller.invoke("computationBoth2", 5)
		controller.invoke("computationBoth2", 5)

		val actionWordCounts = new WordCountAction()
		controller.registerAction("wordCount", actionWordCounts, 1)
		println(controller.invoke("wordCount", "hola que tal"))

		val calculator : Function[T, R] = (cal: CalculatorProxy) => new Calculator()
		//controller.registerAction("calculatorProxy", calculator, 1)
		//val calculatorProxy : CalculatorProxy = DynamicProxy.getActionProxy("calculatorProxy", true)
		//println(calculatorProxy.suma(Map(("x", 2), ("y", 1))))
	}
}

