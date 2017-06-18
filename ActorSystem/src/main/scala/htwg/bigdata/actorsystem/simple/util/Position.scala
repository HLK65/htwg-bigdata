package htwg.bigdata.actorsystem.simple.util

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
case class Position(x: Int, y: Int) {

  override def toString: String = x + "/" + y

  override def equals(o: Any): Boolean = o match {
    case that: Position => that.x == this.x && that.y == this.y
    case _ => false
  }

  override def hashCode: Int = {
    var result = x
    result = 31 * result + y
    result
  }
}
