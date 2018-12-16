import dufflesdsl.model._

package object dufflesdsl {

  /*
   * The DSL syntax is I want `Seq[Resource]` on `String*`
   *
   * Ex: I want Seq(File("/tmp/myfile", "content")) on ("server1", "server2")
   *
   */

  object I {
    def want(resources: Seq[Resource]) = new Targets(resources)
  }

  class Targets(resources: Seq[Resource]) {
    def on(targets: String*) = targets.foreach(
      new Workflow(_).ensure(resources)
    )
  }

}
