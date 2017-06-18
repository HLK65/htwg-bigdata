package htwg.bigdata.actorsystem.httpAnt

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

/**
  * Created by tim on 06.04.17.
  */
object AntSimulation {
  private val counter = new AtomicInteger()
  private val random = scala.util.Random
  implicit val system: ActorSystem= ActorSystem()
  implicit val materializer: Materializer= ActorMaterializer()
  implicit def executor: ExecutionContextExecutor = system.dispatcher
  val config = ConfigFactory.load()
  val mainServerAdresse: String = config.getString("server")
  val antNumber:Int =config.getInt("antNumber")
  val targetPosition: Position = Position(config.getInt("destination.x"),config.getInt("destination.y"))
  def getCounter = counter.incrementAndGet

  def main(args: Array[String]) {

    /**
      * tell mainserver that a new simulation has started
      */
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://" + AntSimulation.mainServerAdresse + "/newsimulation", entity = ""))

    val antSystem = ActorSystem("antsystem")
    println("AntSimulation-Start")
    for (it <- 1 to antNumber) {
      val myActor = antSystem.actorOf(Props(new Ant()))
      val waitDuration = random.nextDouble()
      system.scheduler.scheduleOnce(waitDuration seconds,myActor,"start")
    }
  }
}
