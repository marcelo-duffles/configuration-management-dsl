package examples

import dufflesdsl._
import dufflesmodules._
import dufflesmodules.php

object TwoPHPServers extends App {
  iWant(php)("ubuntu@54.205.233.118", "ubuntu@54.210.111.220")
}
