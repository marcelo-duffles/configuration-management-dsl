package dufflesdsl.model

trait Resource { val name: String }

case class File(
  name:    String,
  content: String,
  owner:   Option[String] = None,
  mode:    Option[String] = None
) extends Resource
case class Service(name: String) extends Resource
case class Package(name: String) extends Resource
