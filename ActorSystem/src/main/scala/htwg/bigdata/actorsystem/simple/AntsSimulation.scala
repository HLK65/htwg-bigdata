package htwg.bigdata.actorsystem.simple

import java.io.{BufferedWriter, File, FileWriter}
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem, Props}
import htwg.bigdata.actorsystem.simple.actors.{Ant, Navigator}
import htwg.bigdata.actorsystem.simple.util.{Position, Timer}
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
  * Created by Michael Walz on 02.04.2017.
  */
object AntsSimulation {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val system = ActorSystem("antSystem")
  private val random = scala.util.Random
  private val timer = new Timer

  def main(args: Array[String]) {

    val positions = mutable.HashMap[ActorRef, Position]()

    // create navigator actor
    val navigator = system.actorOf(Props(new Navigator(positions)), "navigatorActor")

    // ask user to start
    println("Press any key to start simulation.")
    System.in.read

    // create ants and schedule them to ask navigator for collisions
    for (it <- 1 to Presets.MaxAnts) {
      val antPosition = Position(random.nextInt(Presets.SpawnWidth + 1), random.nextInt(Presets.SpawnWidth + 1))
      val antActor = system.actorOf(Props(new Ant(navigator, antPosition)), name = "ant_" + it)
      positions.put(antActor, antPosition)
    }

    // start time measurement
    timer.start
  }

  def exitSimulation(antPositions: mutable.HashMap[ActorRef, Position], collisions: AtomicInteger, kills: AtomicInteger,
                     failedKills: AtomicInteger, movesDone: AtomicInteger): Unit = {

    var result = ""
    if (movesDone.get >= 0 && collisions.get >= 0 && kills.get == Presets.MaxAnts) {
      result = "OK"
    } else {
      result = "NOT OK"
    }

    val strBuilder = new StringBuilder

    strBuilder ++= "\n\n\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "   FieldWidth: " + Presets.FieldWidth + "\n"
    strBuilder ++= "   SpawnWidth: " + Presets.SpawnWidth + "\n"
    strBuilder ++= "      MaxAnts: " + Presets.MaxAnts + "\n"
    strBuilder ++= "  MinVelocity: " + Presets.MinDuration + "\n"
    strBuilder ++= "  MaxVelocity: " + Presets.MaxDuration + "\n"
    strBuilder ++= "FinalPosition: " + Presets.FinalPosition + "\n"
    strBuilder ++= "    ShowBoard: " + Presets.ShowBoard + "\n"
    strBuilder ++= "    ShowStats: " + Presets.ShowStats + "\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "        Moves: " + movesDone + "\n"
    strBuilder ++= "   Collisions: " + collisions + "\n"
    strBuilder ++= " Ants started: " + Presets.MaxAnts + "\n"
    strBuilder ++= "Ants finished: " + kills + "\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "       Result: " + result + "\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "         Time: " + timer.getElapsedTime / 1000000000F + " sec\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "\n\n\n"

    print(strBuilder)

    if (Presets.WriteToFile) {
      val fileName = "simulation_out_" + System.nanoTime
      val file = new File("output/" + fileName + ".txt")
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(strBuilder.toString)
      bw.close()
    }

    System.exit(0)
  }
}