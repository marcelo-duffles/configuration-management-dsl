package dufflesdsl.model

object ResourceSet {
  type ResourceSet = Set[Resource]

  def apply(elems: Resource*): ResourceSet = Set[Resource](elems: _*)
  def fromSeq(elems: Seq[Resource]): ResourceSet = Set[Resource](elems: _*)
}
