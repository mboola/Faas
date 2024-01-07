package scala

import java.util.function.{Function => JFunction}

import core.application.Controller;
import core.invoker.Invoker;
import policymanager.PolicyManager;
import policymanager.RoundRobin;

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

	//TimerDecorator
	//def timerDecorator[T, R](function: T => R): T => R = { 
	//	(arg: T) => {
	//		val startTime = System.nanoTime()
	//		val result = function(arg)
	//		val endTime = System.nanoTime()
	//		println(s"Time of execution is ${endTime - startTime} ns.")
	//		result
	//	}
	//}

	// Cache function, utilizando el mapa mutable de Scala.
	// Locura el getOrElseUpdate. Guarda el argumento (sea el que sea, pudiendo ser una funcion...)
	// Junto con el resultado de su ejecución. 
	//def cacheDecorator[T, R](function: T => R): T => R = {
	//	val cache = collection.mutable.Map[T, R]()
	//	(arg: T) => cache.getOrElseUpdate(arg, function(arg))
	//}

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
	}
}

