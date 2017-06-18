package htwg.bigdata.actorsystem.workers.navigator

import akka.actor.Actor
import htwg.bigdata.actorsystem.simple.Presets
import htwg.bigdata.actorsystem.simple.actors.Messages
import htwg.bigdata.actorsystem.simple.util.Position

/**
  * Created by Michael Walz on 01.04.2017.
  */
class Worker extends Actor {

  private var collisions = 0
  private var kills = 0
  private var movesDone = 0

  var antPositions: Set[Position] = Set()

  override def receive: PartialFunction[Any, Unit] = {

    case workerRequest: Messages.WorkerRequest =>

      println(self + " - Colisions: " + collisions + ", Moves: " + movesDone + ", Kills: " + kills)

      val demandedPosition = workerRequest.antRequest.demandedPosition
      val currentPosition = workerRequest.antRequest.currentPosition
      val antRef = workerRequest.antRef

      if (demandedPosition.x < Presets.FinalPosition.x ||
        demandedPosition.y < Presets.FinalPosition.y) {
        if (causesCollisions(demandedPosition)) {
          collisions += 1
          antRef ! "fieldOccupied"

        } else {
          antPositions -= currentPosition
          antPositions += demandedPosition
          movesDone += 1
          //draw(antPositions, collisions, kills, failedKills, movesDone)
          antRef ! demandedPosition
        }

      } else {
        // ant demands finish position --> kill ant
        antPositions -= currentPosition
        kills += 1
        //draw(antPositions, collisions, kills, failedKills, movesDone)
        antRef ! "kill"

        // TODO: shutdown actor system if all ants have finished
        /*
          if (antPositions.isEmpty) {
            ActorSystem("antSystem").terminate
            NavigatorMaster.exitSimulation(antPositions, collisions, kills, failedKills, movesDone)
          }
        */
      }

    case _ => // do nothing
  }

  def causesCollisions(position: Position): Boolean = {
    antPositions.contains(position)
  }
}