package htwg.bigdata.actorsystem.simple.actors

import akka.actor.ActorRef
import htwg.bigdata.actorsystem.simple.util.Position

/**
  * Created by Michael Walz on 01.04.2017.
  */
object Messages {

  case class FieldOccupied()

  case class Finished()

  case class Result(isPositionAllowed: Boolean)

  case class WorkerRequest(antRequest: AntRequest, antRef: ActorRef)

  case class AntRequest(demandedPosition: Position, currentPosition: Position)

}
