import sbt.Keys._

name := "configuration-management-dsl"
startYear := Some(2018)
scalaVersion := "2.12.8"

libraryDependencies ++= Seq (
   "org.scalatest" %% "scalatest" % "3.0.5" % Test,
   "org.mockito" %% "mockito-scala" % "1.0.5" % Test,

)



