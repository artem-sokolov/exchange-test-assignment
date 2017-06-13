name := "exchange-test-assignment"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.12.2",
  "com.typesafe.akka" % "akka-actor_2.12" % "2.5.2",
  "org.scalatest" % "scalatest_2.12" % "3.0.3" % "test",
  "org.scalacheck" % "scalacheck_2.12" % "1.13.5" % "test",
  "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.2" % "test"
)