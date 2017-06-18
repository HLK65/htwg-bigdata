package htwg.bigdata.actorsystem.workers

import akka.actor.{ActorSystem, Props}
import htwg.bigdata.actorsystem.simple.Presets
import htwg.bigdata.actorsystem.simple.util.{Position, Timer}
import htwg.bigdata.actorsystem.workers.ants.Ant
import htwg.bigdata.actorsystem.workers.navigator.Master

/**
  * Created by Michael Walz on 01.04.2017.
  */
object AntSimulation {

  private val system = ActorSystem("antSystem")
  private val random = scala.util.Random
  private val timer = new Timer

  def main(args: Array[String]) {

    // create navigator actor
    val navigator = system.actorOf(Props(new Master(3)), "masterNavigator")

    // ask user to start
    println("Press any key to start simulation.")
    System.in.read

    for (it <- 1 to Presets.MaxAnts) {
      val antPosition = Position(random.nextInt(Presets.SpawnWidth), random.nextInt(Presets.SpawnWidth))
      system.actorOf(Props(new Ant(navigator, antPosition)), name = "ant_" + it)
    }

    // start time measurement
    timer.start

    println("OK")
  }
}
