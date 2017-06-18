package ants

import java.util.concurrent.atomic.AtomicInteger

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import net.liftweb.json._
import spray.json.{DefaultJsonProtocol, _}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContextExecutor

case class Ant_DTO(id: String, x_current: Int, y_current: Int, x_new: Int, y_new: Int)

case class AppConfiguration(antNo: Int, destX: Int, destY: Int, startX: Int, startY: Int, serverIp: String = "")

case class ServerIp(ip: String)

class AntSimulation extends Simulation with DefaultJsonProtocol {

  implicit val ant_dtoFormat = jsonFormat5(Ant_DTO)
  implicit val appConfigFormat = jsonFormat6(AppConfiguration)
  implicit val serverIpFormat = jsonFormat1(ServerIp)
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val headers_10 = Map("Content-Type" -> """application/json""")
  private val random = scala.util.Random
  val finalPosition = (25, 25)
  val ip = "localhost" //ip des Mainservers
  val mainPort = ":27020" //port des Mainservers
//  val workerPort = ":27021"
  val worker = Array("localhost:27021", "localhost:27022") //Array aller Worker. "ip:port"
  private val counter = new AtomicInteger()
  private val numberOfAnts = 100
  private val antsPerSecond = 10

  Http().singleRequest(HttpRequest(uri = "http://" + ip + mainPort + "/newsimulation", entity = ""))
  Http().singleRequest(HttpRequest(PUT, uri = "http://" + ip + mainPort + "/config", entity = AppConfiguration(numberOfAnts, finalPosition._1, finalPosition._1, 2, 2 /*, ip + workerPort*/).toJson.toString))
  //  Http().singleRequest(HttpRequest(PUT, uri = "http://" + ip + workerPort + "/config", entity = ServerIp(ip + workerPort).toJson.toString))

  for(w <- worker) {
    Http().singleRequest(HttpRequest(uri = "http://" + w + "/newsimulation", entity = ""))
  }


  val httpConf = http
    .baseURL("http://" + ip + mainPort) // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val ants = scenario("Ants Test").exec(Create.create, Move.move)
  val display = scenario("Display Ants Test").exec(Display.display)

  setUp(
    display.inject(atOnceUsers(1)).protocols(httpConf),
    ants.inject(rampUsers(numberOfAnts) over (numberOfAnts / antsPerSecond seconds)).protocols(httpConf)
  )

  //ants.inject(atOnceUsers(numberOfAnts)).protocols(httpConf)
  //  setUp(scn.inject(rampUsers(10000) over(600 seconds)).protocols(httpConf))

  //  object Configure {
  //    val configure = ???
  //  }

  object Create {
    val create = tryMax(Int.MaxValue) {
      exec(http("create ant")
        .post("/ant")
        .check(jsonPath("$.id").saveAs("id"))
        .check(jsonPath("$.x_current").saveAs("current_x"))
        .check(jsonPath("$.y_current").saveAs("current_y"))
      )
        .pause(0.5 seconds, 1 seconds)
    }.exitHereIfFailed
  }

  object Move {
    val move = asLongAs(session => getSessionVar(session, "current_x").toInt < finalPosition._1
      || getSessionVar(session, "current_y").toInt < finalPosition._2) {
      tryMax(Int.MaxValue) {
//        pace(0.1 seconds). //speed
          exec(session => { // calculate target position
            val x = getSessionVar(session, "current_x").toInt
            val y = getSessionVar(session, "current_y").toInt
            val (new_x, new_y) = calculateNewPosition(x, y)
            session.set("new_x", new_x).set("new_y", new_y)
          })
          .exec(http("move ant") // ask for validation
            .put(session => {("http://" + worker(getSessionVar(session, "new_x").toInt % worker.size) + "/ant")})
            .headers(headers_10)
            .body(StringBody(session => {
              val x = getSessionVar(session, "current_x").toInt
              val y = getSessionVar(session, "current_y").toInt
              val new_x = getSessionVar(session, "new_x").toInt
              val new_y = getSessionVar(session, "new_y").toInt
              val id = getSessionVar(session, "id")
              Ant_DTO(id, x, y, new_x, new_y).toJson.toString()
            }))
            .check(status.is(200).saveAs("status"))
          )
        //.pause(0.1 seconds, 1 seconds) //hat keinerlei Auswirkung
      } //.exitHereIfFailed
        .exec(session => { // update position
        if (getSessionVar(session, "status") == "200") {
          session.set("current_x", getSessionVar(session, "new_x")).set("current_y", getSessionVar(session, "new_y"))
        } else {
          session
        }
      })
    }
      .exec(session => {
        counter.incrementAndGet()
        session
      })
  }

  object Display {
    val display = asLongAs(_ => counter.get() < numberOfAnts) {
      exec(http("get ants")
        .get("/ants")
        .check(bodyString.saveAs("ants"))
      )
        .exec(session => {
          println("\n================================================================================\n" + getSessionVar(session, "ants"))
          session
        })
        .pause(1 seconds)
    }
  }

  private def getSessionVar(session: Session, varName: String) = session.get(varName).asOption[String].getOrElse("-1")

  private def calculateNewPosition(x: Int, y: Int): (Int, Int) = {
    val randomInt = if (x >= finalPosition._1) 2 else if (y >= finalPosition._2) 1 else random.nextInt(3)
    randomInt match {
      case 0 => if (x < finalPosition._1 && y < finalPosition._2) return (x + 1, y + 1)
      case 1 => if (x < finalPosition._1) return (x + 1, y)
      case 2 => if (y < finalPosition._2) return (x, y + 1)
    }
    (x, y)
  }
}


