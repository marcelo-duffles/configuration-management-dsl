package dufflesdsl

import org.scalatest.{ WordSpec, Matchers }
import scala.util.{ Try, Failure, Success }
import dufflesdsl.Sensor._
import dufflesdsl.model._

class SensorSpec extends WordSpec with Matchers {

  "The method realityOfASet()" when {

    val set = ResourceSet(
      File("name1", "content1"),
      File("name2", "content2"),
      File("name3", "content3")
    )

    "all the resources are equal to reality" should {
      def fixture = {
        var c = 0
        _: Resource => {
          c += 1
          c match {
            case 1 => Success(Some(File("name1", "content1")))
            case 2 => Success(Some(File("name2", "content2")))
            case _ => Success(Some(File("name3", "content3")))
          }
        }
      }
      "return a Success containing a ResourceSet with all resources" in {
        val realityOfAResource = fixture
        realityOfASet(set, realityOfAResource) shouldBe Success(set)
      }
    }

    "a resource is not present in reality" should {
      def fixture = {
        var c = 0
        _: Resource => {
          c += 1
          c match {
            case 1 => Success(Some(File("name1", "content1")))
            case 2 => Success(None)
            case _ => Success(Some(File("name3", "content3")))
          }
        }
      }
      "return a Success containing a ResourceSet omitting only the missing resource" in {
        val realityOfAResource = fixture
        realityOfASet(set, realityOfAResource) shouldBe Success(ResourceSet(
          File("name1", "content1"),
          File("name3", "content3")
        ))
      }
    }

    "a reality assessment fails for one resource" should {
      val failureInResource2 = Failure(new Throwable("Failure in resource 2"))
      def fixture = {
        var c = 0
        _: Resource => {
          c += 1
          c match {
            case 1 => Success(Some(File("name1", "content1")))
            case 2 => failureInResource2
            case _ => Failure(new Throwable("Failure in resource 3"))
          }
        }
      }
      "lazily return the first Failure encountered" in {
        val realityOfAResource = fixture
        realityOfASet(set, realityOfAResource) shouldBe failureInResource2
      }
    }
  }

  "The method senseFile()" when {

    val file = File("name", "content")

    "the sensed reality is equal to the given File" should {
      "return a Success containing the given File" in {
        senseFile(file)(_ => Ok) shouldBe Success(Some(file))
      }
    }

    "the given File doesn't exist in reality" should {
      def run(cmd: String) = if (cmd.contains("diff")) FileNotFound else Ok
      "return a Success containing None" in {
        senseFile(file)(run) shouldBe Success(None)
      }
    }

    "the content of the given File differs from reality" should {
      def run(cmd: String) = if (cmd.contains("diff")) FileDiff else Ok
      "return a Success containing None" in {
        senseFile(file)(run) shouldBe Success(None)
      }
    }

    "the diff utility returns an unknown status code" should {
      def run(cmd: String) = if (cmd.contains("diff")) ErrUnknown else Ok
      "return a Failure" in {
        senseFile(file)(run) shouldBe a[Failure[_]]
      }
    }

    "the SSH connection fails" should {
      "return a Failure" in {
        senseFile(file)(_ => ErrSSH) shouldBe a[Failure[_]]
      }
    }

    "the tmp file creation fails" should {
      "return a Failure" in {
        senseFile(file)(_ => ErrUnknown) shouldBe a[Failure[_]]
      }
    }

  }
}
