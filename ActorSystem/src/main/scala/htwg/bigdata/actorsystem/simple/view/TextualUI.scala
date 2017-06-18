package htwg.bigdata.actorsystem.simple.view

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorRef
import htwg.bigdata.actorsystem.simple.Presets
import htwg.bigdata.actorsystem.simple.util.Position

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
  * Created by Michael Walz and Fabian Mog on 30.03.2017.
  */
object TextualUI {

  def printBoard(antPositions: mutable.HashMap[ActorRef, Position], collisions: AtomicInteger, kills: AtomicInteger,
                 failedKills: AtomicInteger, movesDone: AtomicInteger, showBoard: Boolean, showStats: Boolean): Unit = {

    if (showBoard) {
      val positions = antPositions.values.toList
      val strBuilder = new StringBuilder
      for (row <- 0 to Presets.FieldWidth) {
        for (col <- 0 to Presets.FieldWidth) {
          if (positions.exists(p => p.x == col && p.y == row)) {
            strBuilder ++= " "
            strBuilder ++= Console.BLACK_B + Console.RED + "@" + Console.BLACK
          } else {
            strBuilder ++= " O"
          }
        }
        strBuilder ++= "\n"
      }
      println
      println(strBuilder.toString)
    }

    if (showStats) {
      println(
        "Moves: " + movesDone + ", Collisions: " + collisions
          + ", Successful kills: " + kills + ", Failed kills: " + failedKills
      )
    }
  }

}
