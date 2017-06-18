package htwg.bigdata.actorsystem.workers.ants

import akka.actor.{Actor, ActorRef, ActorSystem}
import htwg.bigdata.actorsystem.simple.Presets
import htwg.bigdata.actorsystem.simple.actors.Messages
import htwg.bigdata.actorsystem.simple.util.Position

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
class Ant(val navigatorRef: ActorRef, var position: Position) extends Actor {

  private val random = scala.util.Random
  private val system = ActorSystem("antSystem")

  private val duration = if (Presets.MaxDuration == 0) 0 else {
    random.nextInt(Presets.MaxDuration - Presets.MinDuration) + Presets.MinDuration
  }

  private val cancellable =
    system.scheduler.schedule(Duration.Zero, Duration(duration, "millis"))(tellNewPosition)

  override def receive: PartialFunction[Any, Unit] = {
    case pos: Position =>
      // set new position
      position = pos

    case "fieldOccupied" => // do nothing

    case "kill" =>
      // final position reached
      cancellable.cancel
      context.stop(self)

    case _ => println("unknown message")
  }

  private def tellNewPosition() = {

    // init result position with current position
    var result = Position(position.x, position.y)

    // increase x OR y randomly OR (increase x and y)
    var randomInt = random.nextInt(3)

    // x already on border
    if (position.x >= Presets.FinalPosition.x) {
      randomInt = 2
    }

    // y already on border
    if (position.y >= Presets.FinalPosition.y) {
      randomInt = 1
    }

    if (randomInt == 0) {
      if (position.x < Presets.FinalPosition.x && position.y < Presets.FinalPosition.y) {
        result = Position(position.x + 1, position.y + 1)
      }
    } else if (randomInt == 1) {
      if (position.x < Presets.FinalPosition.x) {
        result = Position(position.x + 1, position.y)
      }
    } else if (randomInt == 2) {
      if (position.y < Presets.FinalPosition.y) {
        result = Position(position.x, position.y + 1)
      }
    }

    // ask navigator if new position is empty
    navigatorRef ! Messages.AntRequest(result, position)
  }
}
