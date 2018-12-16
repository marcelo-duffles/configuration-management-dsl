import dufflesdsl.model._

package object dufflesdsl {

  def iWant(resources: Seq[Resource])(targets: String*): Unit = targets.foreach(
    new Workflow(_).ensure(resources)
  )

}
