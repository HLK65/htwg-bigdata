import com.typesafe.sbt.packager.docker.Cmd

name := "akka-http-microservice-mainserver"
organization := "com.theiterators"
version := "1.0"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.3"
  val scalaTestV  = "2.2.6"

  Seq(
    //scala mongodb driver
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.0.0",
    "org.mongodb" %% "casbah" % "3.1.1",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",
    "net.liftweb" %% "lift-json" % "2.6",
    "net.debasishg" %% "redisclient" % "3.4"

  )
}



enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)


dockerBaseImage := "frolvlad/alpine-oraclejdk8"

dockerCommands := dockerCommands.value.flatMap{
  case cmd@Cmd("FROM",_) => List(cmd,Cmd("RUN", "apk update && apk add bash"), Cmd("EXPOSE", "27020"))
  case other => List(other)
}

Revolver.settings

fork in run := true