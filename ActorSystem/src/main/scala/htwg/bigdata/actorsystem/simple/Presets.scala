package htwg.bigdata.actorsystem.simple

import htwg.bigdata.actorsystem.simple.util.Position

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
object Presets {

  // params
  val FieldWidth = 15
  val SpawnWidth = 5
  val MaxAnts = 100
  val FinalPosition = Position(FieldWidth, FieldWidth)

  // ants "ask for new position" interval
  val MinDuration = 50
  val MaxDuration = 200

  // print options
  val ShowBoard = true
  val ShowStats = false
  val ShowProgressBar = false
  val WriteToFile = true
}
