import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.{Cmd, DockerPlugin}
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

name := "ActorSystem"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.8"
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.17"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http-jackson" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http-xml" % "10.0.5"
// https://mvnrepository.com/artifact/net.liftweb/lift-webkit_2.10
libraryDependencies += "net.liftweb" % "lift-webkit_2.10" % "2.6.3"

//enablePlugins(JavaAppPackaging)
//enablePlugins(DockerPlugin)
//
//dockerBaseImage := "frolvlad/alpine-oraclejdk8"
//
//dockerCommands := dockerCommands.value.flatMap{
//  case cmd@Cmd("FROM",_) => List(cmd,Cmd("RUN", "apk update && apk add bash"))
//  case other => List(other)
//}

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    name := "ActorSystem",
    mainClass in (Compile) := Some("htwg.bigdata.actorsystem.httpAnt.AntSimulation"),
    version := "1.0",
    dockerBaseImage := "azul/zulu-openjdk:8",
    dockerUpdateLatest := true,
    dockerExposedPorts := Seq(9000)
  )