package dufflesmodules

import dufflesdsl._
import dufflesdsl.model._

package object dufflesmodules {
  def php = Seq(
    Package("apache2"),
    Package("php5"),
    File(name = "/var/www/html/index.php",
        content = """<?php
                    |
                    |header("Content-Type: text/plain");
                    |
                    |echo "Hello, world!\n";""".stripMargin,
        owner = Some("ubuntu"),
        mode = Some("644")
    ),
  )
}
