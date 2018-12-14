import dufflesdsl.model._

package object dufflesdsl extends Sensor with Modifier {

  def iWant(resources: Seq[Resource])(targets: String*): Unit = targets.foreach(
    new Workflow(_).ensure(resources)
  )

}
