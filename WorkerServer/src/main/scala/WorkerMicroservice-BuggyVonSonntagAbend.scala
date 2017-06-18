/*
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.DELETE
import akka.http.scaladsl.model.HttpMethods.PUT
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import net.liftweb.json._
import spray.json.DefaultJsonProtocol

import scala.collection.JavaConversions._
import scala.collection.SortedMap.Default
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future, Promise}

case class Ant_DTO(id: String, x_current: Int, y_current: Int, x_new: Int, y_new: Int)

case class Position(x: Int, y: Int)

case class ServerIp(ip: String)

trait Service extends DefaultJsonProtocol {
  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val formats = DefaultFormats

  implicit def executor: ExecutionContextExecutor

  val logger: LoggingAdapter

  //  var knownAnts: mutable.HashSet[Int] = mutable.HashSet[Int]()
  val routes = {
    logRequestResult("akka-http-microservice") {

      pathPrefix("newsimulation") {
        get {
          positionSet = mutable.HashSet[Position]()
          //          knownAnts = mutable.HashSet[Int]()
          complete(StatusCodes.OK)
        }
      } ~ pathPrefix("ant") {
        pathEnd {
          /*  Move Request
          *   on illegal move, return 403
          *   on legal move, send delete to old workerserver, move to main and return 200
          * */
          put {
            decodeRequest {
              entity(as[String]) { content: String =>
                val json = parse(content)
                val ant = json.extract[Ant_DTO]
                //println(ant.toString)
                var statusCode = 0
                val position: Position = Position(ant.x_new.toInt, ant.y_new.toInt)
                //                val blocker = Promise[Unit]()
                //                val futureBlocker = blocker.future

                // if move is move on destination, delete on worker and main and return ok
                if (position.x == destination_x && position.y == destination_y) {
                  positionSet.remove(Position(ant.x_current, ant.y_current))
                  Http().singleRequest(HttpRequest(DELETE, uri = "http://" + mainServer + "/ant/" + ant.id, entity = compact(render(json))))
                  statusCode = StatusCodes.OK.intValue
                  /*
                  //                  println("ant " + ant.id + "finished")
                  val responsibleServerNumberDelete = ant.x_current % numberOfServer
                  val serverUriDelete = ipAddressMap(responsibleServerNumberDelete)


                  //update visualisation/ delete on main
                  Http().singleRequest(HttpRequest(DELETE, uri = "http://" + mainServer + "/ant/" + ant.id, entity = compact(render(json))))

                  //delete on old workerserver
                  val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(DELETE, uri = "http://" + serverUriDelete + "/ant", entity = compact(render(json))))
                  for (response <- responseFuture) {
                    response.status match {
                      // delete successful
                      case StatusCodes.OK => {
                        statusCode = StatusCodes.OK.intValue
                      }
                      case _ => {
                        println("delete failed")
                        statusCode = StatusCodes.BadRequest.intValue
                      }
                    }
                    blocker.success()
                  }*/
                }
                // check if move is legal or not
                else if (positionSet.add(position)) {
                  var bool = positionSet.remove(Position(ant.x_current, ant.y_current))
                  if (!bool && ant.x_current > 5 && ant.y_current > 5) {
                    println("################## positionSet remove failed", positionSet)


                  }
                  Http().singleRequest(HttpRequest(PUT, uri = "http://" + mainServer + "/ant/" + ant.id, entity = compact(render(json))))
                  statusCode = StatusCodes.OK.intValue

                  /*
                  //                  println("in else if")
                  val responsibleServerNumberDelete = ant.x_current % numberOfServer
                  val serverUriDelete = ipAddressMap(responsibleServerNumberDelete)

                  //delete on old workerserver
                  //                  println("delete on old worker" + serverUriDelete)

                  Http().singleRequest(HttpRequest(PUT, uri = "http://" + mainServer + "/ant/" + ant.id, entity = compact(render(json))))

                  val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(DELETE, uri = "http://" + serverUriDelete + "/ant", entity = compact(render(json))))
                  for (response <- responseFuture) {
                    response.status match {
                      // update successful
                      case StatusCodes.OK => {
                        // deleted on old workerserver and mainserver updated, set status to ok
                        statusCode = StatusCodes.OK.intValue
                      }
                      case _ => {
                        //                              println("mainserver update failed: " + response)
                        statusCode = StatusCodes.BadRequest.intValue
                      }
                    }
                    blocker.success()
//                    knownAnts.add(ant.id.toInt) //erst nach erfolgter deletion ausführen falls worker old und new die selben sind
                  }
                 */
                } else {
                  statusCode = StatusCodes.Forbidden.intValue
                  /*blocker.success()*/
                }

                //Antwort an Ant freigeben und absenden
                /*Await.result(futureBlocker, Duration.Inf)*/
                complete(statusCode, "")

              }
            }
          } ~
            delete {
              decodeRequest {
                entity(as[String]) { content: String =>
                  //                  println(content)
                  val jValue = parse(content)
                  val ant = jValue.extract[Ant_DTO]
                  var statusCode = 0
                  val position: Position = Position(ant.x_current.toInt, ant.y_current.toInt)
                  if (positionSet.remove(position)) {
                    //                    knownAnts.remove(ant.id.toInt)
                    statusCode = StatusCodes.OK.intValue
                    //                  } else if (!(knownAnts.contains(ant.id.toInt))) { // wenn ant gerade frisch gespawnt wurde und noch nie gesetzt wurde
                    //                    statusCode = StatusCodes.OK.intValue
                  } else if (!positionSet.contains(position)) {
                    statusCode = StatusCodes.OK.intValue
                  } else {
                    println("positions", positionSet.toString(), "x", ant.x_current, "y", ant.y_current, "id", ant.id.toInt)
                    statusCode = StatusCodes.NotFound.intValue
                  }
                  complete(statusCode, "")
                }
              }
            }
        }
      } ~ pathPrefix("config") {
        pathEnd {
          put {
            decodeRequest {
              entity(as[String]) { content: String =>
                println(content)
                println("in put config on worker")
                /*wird nicht aufgerufen*/
                val json = parse(content)
                val appConfiguration = json.extract[ServerIp]
                this.serverIp = appConfiguration.ip
                complete(StatusCodes.OK.intValue, "")
              }
            }
          }
        }
      } ~ pathPrefix("ants") {
        pathEnd {
          get {
            val positions = positionSet
            val strBuilder = new StringBuilder
            for (row <- 0 to destination_x) {
              for (col <- 0 to destination_y) {
                if (positions.exists(p => p.x == col && p.y == row)) {
                  strBuilder ++= " "
                  strBuilder ++= "@ "
                } else {
                  strBuilder ++= " . "
                }
              }
              strBuilder ++= "\n"
            }
            complete(strBuilder.toString)
          }

        }
      }
    }
  }
  var numberOfServer: Int = 0
  var serverIp = ""
  var ipAddressMap: mutable.HashMap[Int, String] = mutable.HashMap[Int, String]()
  var mainServer = ""
  var destination_x = 20
  var destination_y = 20

  def config: Config
}

object WorkerMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  numberOfServer = config.getStringList("servers").size()
  //  serverIp = config.getStringList("servers").head
  mainServer = config.getString("mainserver")
  destination_x = config.getInt("destination.x")
  destination_y = config.getInt("destination.y")

  for ((ipAddress, id) <- config.getStringList("servers").zipWithIndex) {
    ipAddressMap.put(id, ipAddress)
  }
  println("Worker on Port " + config.getInt("http.port"))
  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
*/