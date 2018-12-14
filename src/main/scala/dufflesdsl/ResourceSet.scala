package dufflesdsl

import dufflesdsl.model._

object ResourceSet {
  type ResourceSet = Set[Resource]

  def apply(elems: Resource*) = Set[Resource](elems: _*)
  def fromSeq(elems: Seq[Resource]) = Set[Resource](elems: _*)
}

