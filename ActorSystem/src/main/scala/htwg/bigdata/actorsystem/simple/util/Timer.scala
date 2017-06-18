package htwg.bigdata.actorsystem.simple.util

/**
  * Created by Michael Walz on 30.03.2017.
  */
class Timer {

  private var now = 0L

  def start(): Unit = {
    now = System.nanoTime
  }

  def getElapsedTime: Long = {
    System.nanoTime - now
  }
}
