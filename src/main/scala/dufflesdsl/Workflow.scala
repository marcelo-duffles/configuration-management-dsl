package dufflesdsl

import scala.sys.process._
import scala.util.{ Failure, Success, Try }

import dufflesdsl.model._
import dufflesdsl.model.ResourceSet._

class Workflow(
  target: String
) extends Sensor with Modifier {

  implicit def executionContext(cmd: String) = Status(s"ssh $target $cmd" ! logger)

  val ensure = (toResourceSet andThen diffWithReality andThen doChanges andThen report)(_)
  private val toResourceSet: Seq[Resource] => ResourceSet = ResourceSet.fromSeq(_)
  private val diffWithReality: ResourceSet => Try[ResourceSet] = desired =>
    realityOf(desired) map (desired diff _)
  private val doChanges: Try[ResourceSet] => Try[ResourceSet] = _ flatMap modify
  private val report: Try[ResourceSet] => Unit = {
    case Success(r) => r.size match {
      case 0 => println(s"[$target] All resources are up-to-date.")
      case _ => println(s"[$target] Modified resources: " + r.mkString(", "))
    }
    case Failure(e) => println(s"[$target] ${e.getMessage}")
  }
  private val logger = ProcessLogger(_ => ()) // not used at the moment

}
