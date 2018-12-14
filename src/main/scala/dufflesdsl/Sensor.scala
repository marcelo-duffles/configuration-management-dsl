package dufflesdsl

import scala.util.{ Try, Failure, Success }
import scala.sys.process._
import dufflesdsl.model._
import dufflesdsl.ResourceSet._

private[dufflesdsl] trait Sensor extends BaseDriver {

  def realityOf(set: ResourceSet)(implicit run: String => Status): Try[ResourceSet] =
    realityOfASet(set, realityOfAResource)

  def realityOfASet(set: ResourceSet, realityOf: Resource => Try[Option[Resource]]): Try[ResourceSet] =
    set.foldLeft(Success(ResourceSet())) { (acc, resource) =>
      realityOf(resource) match {
        case s: Success[Option[Resource]] => s.get match {
          case sr: Some[Resource] => Success(acc.get + sr.get)
          case _                  => Success(acc.get)
        }
        case f: Failure[_] => return Failure(f.exception)
      }
    }

  def realityOfAResource(resource: Resource)(implicit run: String => Status): Try[Option[Resource]] =
    resource match {
      case f: File    => Sensor.senseFile(f)
      case p: Package => Sensor.sensePackage(p)
      case r          => Failure(new IllegalArgumentException("sensor not implemented for " + r.name))
    }

}

private object Sensor {

  def senseFile(f: File)(implicit run: String => Status): Try[Option[File]] = createTmpFile(f).flatMap(compare)
  def sensePackage(p: Package)(implicit run: String => Status): Try[Option[Package]] = checkOS(p).flatMap(queryPackage)

  def checkOS(p: Package)(implicit run: String => Status): Try[Package] = {
    run("cat /etc/os-release | grep Ubuntu") match {
      case Ok     => Success(p)
      case ErrSSH => Failure(new RuntimeException(sshErrMsg))
      case _      => Failure(new RuntimeException("OS not supported"))
    }
  }

  def queryPackage(p: Package)(implicit run: String => Status): Try[Option[Package]] = {
    run(s"dpkg-query --status ${p.name} | grep Status | grep installed") match {
      case Ok     => Success(Some(p))
      case ErrSSH => Failure(new RuntimeException(sshErrMsg))
      case _      => Success(None)
    }
  }

  def compare(f: File)(implicit run: String => Status): Try[Option[File]] = {
    run(s"diff ${f.name} ${tmp(f.name)}") match {
      case Ok                      => Success(Some(f))
      case FileNotFound | FileDiff => Success(None)
      case ErrSSH                  => Failure(new RuntimeException(sshErrMsg))
      case ErrUnknown              => Failure(new RuntimeException(diffErrMsg))
    }
  }

  def createTmpFile(f: File)(implicit run: String => Status): Try[File] = {
    run("echo '" + f.content + "' > " + tmp(f.name)) match {
      case Ok     => Success(f)
      case ErrSSH => Failure(new RuntimeException(sshErrMsg))
      case _      => Failure(new RuntimeException(tmpFileCreationErrMsg))
    }
  }

  def tmp(s: String) = "/tmp/dufflesdsl" + s.replace("/", "-")

  val diffErrMsg = "The diff utility returned an unknown status"
  val tmpFileCreationErrMsg = "Unable to create tmp file, something is odd on the target server"

}
