package scala

import java.util.function.{Function => JFunction}
import scala.io.Source

import core.application.Controller;
import core.invoker.Invoker;
import core.dynamicproxy.DynamicProxy;
import policymanager.PolicyManager;
import policymanager._;
import core.application.Action;
import services.proxies.Calculator;
import services.proxies.CalculatorProxy;
import scala.jdk.CollectionConverters._

object Main {
	
	//example function for testing.
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
			val words = text.toLowerCase.split("\\W+").filter(_.nonEmpty)
			words.groupBy(identity).view.mapValues(_.length).toMap
		}
	}

	class CountWordsAction extends Action[String, Map[String, Int]] {
		override def apply(text: String): Map[String, Int] = {
			val words = text.split("\\s+")
			Map("total" -> words.length)
		}
	}

	class ReduceAction extends Action[List[Map[String, Int]], Map[String, Int]] {
		override def apply(list: List[Map[String, Int]]): Map[String, Int] = {
			list.foldLeft(Map.empty[String, Int]) { (acc, map) =>
				acc ++ map.map { case (word, count) => 
					word -> (acc.getOrElse(word, 0) + count)
				}
			}
		}
	}

	//unused but perfectly functional
	def wordCount(text: String): Map[String, Int] = {
		val words = text.toLowerCase.split("\\W+").filter(_.nonEmpty)
		words.groupBy(identity).view.mapValues(_.length).toMap
	}
	
	//unused but perfectly functional
	def countWords(text: String): Map[String, Int] = {
		val words = text.split("\\s+")
		Map("total" -> words.length)
	}

	def reduce(list: List[Map[String, Int]]): Map[String, Int] = {
		list.foldLeft(Map.empty[String, Int]) { (acc, map) =>
			acc ++ map.map { case (word, count) => 
				word -> (acc.getOrElse(word, 0) + count)
			}
		}
	}

	def splitFileIntoParts(filePath: String, numParts: Int): List[String] = {
		val fileContents = Source.fromFile(filePath).mkString
		val partLength = Math.ceil(fileContents.length.toDouble / numParts).toInt
		fileContents.grouped(partLength).toList
	}

	def main(args: Array[String]): Unit = {

		val policyManagerName = "RoundRobin"

		val policyManager: PolicyManager = policyManagerName match {
		case "RoundRobin"   => new RoundRobin()
		case "GreedyGroup"  => new GreedyGroup()
		case "UniformGroup" => new UniformGroup()
		case "BigGroup"     => new BigGroup()
		case _              => new RoundRobin() // Por defecto, usa RoundRobin
		}
		
		val controller = Controller.instantiate()
		controller.setPolicyManager(policyManager)
		val invoker = Invoker.createInvoker(1, 4);
		controller.registerInvoker(invoker)
		//end of initialization 

		//start of automatic testing
		System.out.println("\n======AUTOMATIC TEST START========")
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

		System.out.println("REDUCE TESTS:")

		val actionWordCounts = new WordCountAction()
		val decorated1 = new CacheDecorator(actionWordCounts)
		val decorated = new TimerDecorator(decorated1)
		controller.registerAction("wordCountDecorated", decorated, 1)
		controller.registerAction("wordCount", actionWordCounts, 1)

		System.out.println("Sin Cachear:")
		println(controller.invoke("wordCountDecorated", "hola que tal"))
		System.out.println("Cacheada:")
		println(controller.invoke("wordCountDecorated", "hola que tal"))

		val actionCountWords = new CountWordsAction()
		controller.registerAction("countWords", actionCountWords, 1)
		println(controller.invoke("countWords", "hola que tal tal"))

		val actionReduce = new ReduceAction()
		controller.registerAction("reduce", actionReduce, 1)
		System.out.println("======AUTOMATIC TEST END========")
		//end of automatic testing

		//start of program
		var endLoop = false

		while (!endLoop) {
			println("select an option:")
			println("\t1 - execute wordCount on textfile")
			println("\t2 - execute countWord on textfile")
			println("\t3 - exit program")

			var intValue = scala.io.StdIn.readInt()

			intValue match {
				case 1 =>
					var content = splitFileIntoParts("src/main/scala/input.txt",10)
					val result: List[Map[String, Int]] = controller.invoke("wordCount", content.asJava).asScala.toList
					val reduced: Map[String, Int] = controller.invoke("reduce", result)
					println(reduced)
				case 2 => 
					var content = splitFileIntoParts("src/main/scala/input.txt",10)
					val result: List[Map[String, Int]] = controller.invoke("countWords", content.asJava).asScala.toList
					val reduced: Map[String, Int] = controller.invoke("reduce", result)
					println(reduced)
				case 3 =>
					println("Program ended")
					endLoop = true
				case _ => println("Invalid option, please try again.")
			}
		}

	}
}

