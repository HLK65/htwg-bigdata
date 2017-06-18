package de.htwg.bigdata.visual.resource

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import net.liftweb.json._
import de.htwg.bigdata.visual.spark.DataProcessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class GridRequest(collection: String, x: Int, y: Int, timestep: Int)

object GridResource extends java.io.Serializable {
  implicit val formats = DefaultFormats
 
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("grid") {
        decodeRequest {
          (post & entity(as[String])) { gridRequest: String =>
            val json = parse(gridRequest)
            val mapper = new ObjectMapper() with ScalaObjectMapper
            mapper.registerModule(DefaultScalaModule)
            val grid = mapper.readValue(gridRequest, classOf[GridRequest])

            //val gridrequest = json.extract[GridRequest]
            
            val dataProcessor = new DataProcessor
            val newGrid = dataProcessor.transformGrid(grid)

            complete {
              HttpResponse(entity = newGrid.toString())
            }
          }
        }


      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9500)

    println(s"Server online at http://localhost:9500/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}






