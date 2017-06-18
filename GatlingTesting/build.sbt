enablePlugins(GatlingPlugin)

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.5" % "test,it"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % "2.2.5" % "test,it"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http-jackson" % "10.0.5"
libraryDependencies += "net.liftweb" % "lift-webkit_2.10" % "2.6.3"
