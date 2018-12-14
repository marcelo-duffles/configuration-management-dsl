package dufflesdsl

import dufflesdsl.model._
import dufflesdsl.ResourceSet._
import scala.util.{ Try, Failure, Success }

trait Modifier extends BaseDriver {

  def modify(set: ResourceSet)(implicit run: String => Status): Try[ResourceSet] =
    modifyASet(set, modifyAResource)

  def modifyASet(set: ResourceSet, modify: Resource => Try[Resource]): Try[ResourceSet] =
    set.foldLeft(Success(ResourceSet())) { (acc, resource) =>
      modify(resource) match {
        case s: Success[Resource] => Success(acc.get + s.get)
        case f: Failure[_]        => return Failure(f.exception)
      }
    }

  def modifyAResource(resource: Resource)(implicit run: String => Status): Try[Resource] =
    resource match {
      case f: File    => Modifier.modifyFile(f)
      case p: Package => Modifier.modifyPackage(p)
      case r          => Failure(new IllegalArgumentException("modifier not implemented for " + r.name))
    }
}

private object Modifier {

  def modifyFile(f: File)(implicit run: String => Status): Try[File] = (updateOwner _ andThen updateContent)(f)

  def updateContent(f: File)(implicit run: String => Status): Try[File] = {
    run("sudo echo '" + f.content + "' > " + f.name) match {
      case Ok     => Success(f)
      case ErrSSH => Failure(new RuntimeException(sshErrMsg))
      case _      => Failure(new RuntimeException(fileModificationErrMsg))
    }
  }

  def updateOwner(f: File)(implicit run: String => Status): File = {
    f.owner match {
      case Some(owner) => run(s"sudo touch ${f.name} && sudo chown $owner ${f.name}")
      case None        =>
    }
    f
  }

  def modifyPackage(p: Package)(implicit run: String => Status): Try[Package] = {
    run(s"sudo apt-get install -y ${p.name}") match {
      case Ok     => Success(p)
      case ErrSSH => Failure(new RuntimeException(sshErrMsg))
      case _      => Failure(new RuntimeException(s"Unable to install ${p.name}"))
    }
  }

  val fileModificationErrMsg = "Unable to modify file, something is odd on the target server"
}
