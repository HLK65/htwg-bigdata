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
  var positionSet: mutable.HashSet[Position] = mutable.HashSet[Position]()
  val routes = {
    logRequestResult("akka-http-microservice") {

      pathPrefix("newsimulation") {
        get {
          positionSet = mutable.HashSet[Position]()
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
                val position: Position = Position(ant.x_new, ant.y_new)
                val blocker = Promise[Unit]()
                val futureBlocker = blocker.future

                // if move is move on destination, delete on worker and main and return ok
                if (
                /*/*Alter Code*/position.x == destination_x && position.y == destination_y*/
                /*/*Eugens Code*/position.x == destination_x - (area - 1) && position.y == destination_y - (area - 1)
                  || position.x == destination_x - (area - 1) && position.y == destination_y
                  || position.x == destination_x && position.y == destination_y - (area - 1)*/

                /*Lukas Code*/
                  position.x > destination_x - area && position.y > destination_y - area
                ) {
                  println("ant " + ant.id + "finished")
                  val responsibleServerNumberDelete = ant.x_current % numberOfServer
                  val serverUriDelete = ipAddressMap(responsibleServerNumberDelete)


                  //delete on old workerserver
                  val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(DELETE, uri = "http://" + serverUriDelete + "/ant", entity = compact(render(json))))
                  for (response <- responseFuture) {
                    response.status match {
                      // delete successful
                      case StatusCodes.Accepted => {

                        statusCode = StatusCodes.OK.intValue

                        //update visualisation/ delete on main
                        Http().singleRequest(HttpRequest(DELETE, uri = "http://" + mainServer + "/ant/" + ant.id, entity = compact(render(json))))

                      }
                      case _ => {
                        statusCode = StatusCodes.BadRequest.intValue
                      }
                    } //end match
                    blocker.success()
                  } //end for response
                } //end position is destination

                // check if move is legal or not
                else if (positionSet.add(position)) {
                  val responsibleServerNumberDelete = ant.x_current % numberOfServer
                  val serverUriDelete = ipAddressMap(responsibleServerNumberDelete)

                  /*delete on old workerserver */
                  val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(DELETE, uri = "http://" + serverUriDelete + "/ant", entity = compact(render(json))))
                  for (response <- responseFuture) {
                    response.status match {
                      // delete successful
                      case StatusCodes.Accepted => {

                        /* update mainserver */
                        val responseFutureUpdate: Future[HttpResponse] = Http().singleRequest(HttpRequest(PUT, uri = "http://" + mainServer + "/ant/" + ant.id, entity = compact(render(json))))
                        val blockerUpdate = Promise[Unit]()
                        val futureBlockerUpdate = blockerUpdate.future

                        for (response <- responseFutureUpdate) {
                          response.status match {
                            // update successful
                            case StatusCodes.OK => {
                              // deleted on old workerserver and mainserver updated, set status to ok
                              statusCode = StatusCodes.OK.intValue
                            }
                            case _ => {
                              println("mainserver update failed, statuscode: " + response)
                              positionSet.remove(position) //add aus else if rückgängig machen
                              statusCode = StatusCodes.BadRequest.intValue
                            }
                          }
                          blockerUpdate.success()
                        } //end response update mainserver
                        Await.result(futureBlockerUpdate, Duration.Inf)
                      } //end case delete on worker accepted
                      case _ => {
                        println("not found, statuscode: " + response)
                        positionSet.remove(position) //add aus else if rückgängig machen
                        statusCode = StatusCodes.BadRequest.intValue
                      }
                    } //end match response.status
                    blocker.success()
                  } // end for responseFuture
                } // end else if position.add

                else {
                  statusCode = StatusCodes.Forbidden.intValue
                  blocker.success()
                }

                Await.result(futureBlocker, Duration.Inf)
                complete(statusCode, "")
              }
            }
          } ~
            delete {
              decodeRequest {
                entity(as[String]) { content: String =>
                  println(content)
                  val jValue = parse(content)
                  val ant = jValue.extract[Ant_DTO]
                  var statusCode = 0
                  val position: Position = Position(ant.x_current, ant.y_current)
                  if (positionSet.remove(position)) {
                    statusCode = StatusCodes.Accepted.intValue
                  } else if (!(positionSet.contains(position))) { // Wenn ants gespawnt sind und ersten move machen sind sie noch in keinem positionSet
                    statusCode = StatusCodes.Accepted.intValue
                  } else {
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
        // visualizes ants on this worker. Not of all workers! Useful for debugging
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
  var area = 0

  def config: Config
}

object WorkerMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  numberOfServer = config.getStringList("servers").size()
  serverIp = config.getStringList("servers").head
  mainServer = config.getString("mainserver")
  destination_x = config.getInt("destination.x")
  destination_y = config.getInt("destination.y")
  area = config.getInt("destination.area")

  for ((ipAddress, id) <- config.getStringList("servers").zipWithIndex) {
    ipAddressMap.put(id, ipAddress)
  }
  println("Worker on Port " + config.getInt("http.port"))
  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}