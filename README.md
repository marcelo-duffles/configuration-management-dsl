# configuration-management-dsl
This is a simple prototype of a Puppet-like type-safe DSL written in Scala. Here you can actually **code** your environment, instead of spending your days editing configuration files! Type-safety means that in several cases you know if your code will work even before running it. Cool, right? So continue reading to see how to use it.


## Running one example
You can run the example app to configure two PHP servers:

```
$ sbt run
[...]
[info] Running examples.TwoPHPServers
[ubuntu@54.205.233.118] Modified resources: Package(apache2), Package(php5), File(/var/www/html/index.php,<?php

header("Content-Type: text/plain");

echo "Hello, world!\n";,Some(ubuntu),Some(644))
[ubuntu@54.210.111.220] Modified resources: Package(apache2), Package(php5), File(/var/www/html/index.php,<?php

header("Content-Type: text/plain");

echo "Hello, world!\n";,Some(ubuntu),Some(644))
[success] Total time: 52 s, completed Dec 14, 2018 7:49:11 AM

```

The above execution shows that the application installed the packages `apache2` and `php5` and the sample PHP app in the file `/var/www/html/index.php` on the targets servers. The code snippet below shows the desired state declared by the user:

```
object TwoPHPServers extends App {
  iWant(php)("ubuntu@54.205.233.118", "ubuntu@54.210.111.220")
}
```

The configuration management tool took care of ensuring the desired state matched the reality.


## Developing with the DSL

### Modules

You can build your own modules, here's the `php` module we just used:

```
package object dufflesmodules {
  def php = Seq(
    Package("apache2"),
    Package("php5"),
    File(
      name = "/var/www/html/index.php",
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
```

As you can see, this is plain Scala code.

### Resources

The building blocks are the `Resource`s defined `src/main/scala/dufflesdsl/model/Resource.scala`.


## Internals

### Sensors

A `Sensor` is a software component responsible for assessing the reality for each of the resources declared. Its output is used to determine which resources need to be updated. It's also important for the idempotent execution model. You can see the existing implementation in `src/main/scala/dufflesdsl/Sensor.scala`

### Modifiers

A `Modifier` applies the configurations that are missing in reality, but declared by the user. The implementation is `src/main/scala/dufflesdsl/Modifier.scala`

### Data model and workflow

The data unit is a `ResourceSet` (see `src/main/scala/dufflesdsl/model/ResourceSet.scala`), which is initially built from the user input, then it goes through a functional pipeline defined in `src/main/scala/dufflesdsl/Workflow.scala`. The entire pipeline can be well summarized by this function composition `toResourceSet andThen diffWithReality andThen doChanges andThen report` used by the `Workflow`.

## Testing

You can run unit tests as below:

```
$ sbt test
[...]
[info] SensorSpec:
[info] The method realityOfASet()
[info]   when all the resources are equal to reality
[info]   - should return a Success containing a ResourceSet with all resources
[info]   when a resource is not present in reality
[info]   - should return a Success containing a ResourceSet omitting only the missing resource
[info]   when a reality assessment fails for one resource
[info]   - should lazily return the first Failure encountered
[info] The method senseFile()
[info]   when the sensed reality is equal to the given File
[info]   - should return a Success containing the given File
[info]   when the given File doesn't exist in reality
[info]   - should return a Success containing None
[info]   when the content of the given File differs from reality
[info]   - should return a Success containing None
[info]   when the diff utility returns an unknown status code
[info]   - should return a Failure
[info]   when the SSH connection fails
[info]   - should return a Failure
[info]   when the tmp file creation fails
[info]   - should return a Failure
[info] Run completed in 581 milliseconds.
[info] Total number of tests run: 9
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 9, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 8 s, completed Dec 14, 2018 8:24:55 AM
```
