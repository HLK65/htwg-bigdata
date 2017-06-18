name := "SparkVirtualization"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.5"
libraryDependencies += "net.liftweb" % "lift-webkit_2.10" % "2.6.3"

libraryDependencies ++= Seq(
  "org.mongodb.spark" %% "mongo-spark-connector" % "2.0.0",
  "org.apache.spark" %% "spark-core" % "2.0.0",
  "org.apache.spark" %% "spark-sql" % "2.0.0"
  
  
)