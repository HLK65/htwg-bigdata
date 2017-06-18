package htwg.bigdata.actorsystem.workers.navigator

import akka.actor.{Actor, ActorRef, Props}
import htwg.bigdata.actorsystem.simple.actors.Messages

import scala.collection.mutable

class Master(val numberOfWorkers: Int) extends Actor {

  private val workers: mutable.HashMap[Int, ActorRef] = mutable.HashMap()

  override def receive: Receive = {

    case antRequest: Messages.AntRequest =>

      val targetWorkerNumber = antRequest.demandedPosition.y % numberOfWorkers

      if (!workers.contains(targetWorkerNumber)) {

        // create worker
        workers.put(targetWorkerNumber,
          context.actorOf(Props[Worker], name = "worker" + targetWorkerNumber))
      }

      // divide and conquer
      workers(targetWorkerNumber) !
        Messages.WorkerRequest(antRequest, sender)

    case _ => // do nothing
  }
}