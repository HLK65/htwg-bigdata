
import akka.http.scaladsl.server.util.TupleOps.enhanceTuple
import com.redis._
import org.mongodb.scala.bson.{BsonInt32, BsonString}
import org.mongodb.scala.{Completed, FindObservable, MongoClient, MongoCollection, MongoDatabase, Observable, Observer, SingleObservable}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import com.typesafe.config
import com.typesafe.config.ConfigFactory

/**
  * TODO
  *
  * Redis
  * https://github.com/debasishg/scala-redis
  *
  * Install Redis on Windows: https://github.com/MSOpenTech/redis/releases -> Redis-x64-3.2.100.msi
  * Start Server CMD: redis-cli, redis-cli shutdown, redis-server
  * Monitor Server CMD: redis-cli monitor
  */
object Database {

  val config = ConfigFactory.load()
  var mongoClient: MongoClient = MongoClient("mongodb://" + config.getString("database"))
  val startTimeMillis = System.currentTimeMillis
  var collectionName = ""


  def updateAnt(id: String, x: Int, y: Int) {
    val doc: Document = Document("timestamp" -> (System.currentTimeMillis - startTimeMillis), "id" -> id, "x" -> x, "y" -> y)


    val database: MongoDatabase = mongoClient.getDatabase("ants")

    val collection: MongoCollection[Document] = database.getCollection(collectionName)

    collection.insertOne(doc)
      .subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = return

        override def onError(e: Throwable): Unit = println("Database insert failed")

        override def onComplete(): Unit = return
      })
  }

  def generateNewCollectionName(): Unit = {
    val highestNumber = DatabaseRead.getHighestCollectionNamesNumber()
    collectionName = "collection" + (highestNumber.toInt + 1)
    println("new collectionname: " + collectionName)
    //    val config = ConfigFactory.load()
    val doc: Document = Document("rows" -> config.getString("fieldWith.rows").toInt, "columns" -> config.getString("fieldWith.columns").toInt, "destX" -> config.getString("destination.x").toInt, "destY" -> config.getString("destination.y").toInt)

    val database: MongoDatabase = mongoClient.getDatabase("ants")

    val collection: MongoCollection[Document] = database.getCollection(collectionName)

    collection.insertOne(doc)
      .subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = return

        override def onError(e: Throwable): Unit = println("Failed")

        override def onComplete(): Unit = return
      })

  }

  def readAnts(): Unit = {
    /*

        val database: MongoDatabase = mongoClient.getDatabase("ants")

        val collection: MongoCollection[Document] = database.getCollection("ants")

        val observable: Observable[Document] = collection.find(exists("timestamp")).sort(ascending("timestamp"))


        observable.subscribe(new Observer[Document] {
          override def onNext(result: Document): Unit = {
            //println("onNext")
            //println(result.toJson())
          }

          override def onError(e: Throwable): Unit = println("Failed" + e.getMessage)

          override def onComplete(): Unit = {
            println("Completed")

          }
        })

        for (document: Document <- observable) {
          println(document.toJson())
          //var timestamp = document.get[BsonString]("id")

          var id = document.get[BsonString]("id") map (_.asString().getValue)

          var x = document.get[BsonInt32]("x") map (_.asInt32().getValue)
          var y = document.get[BsonInt32]("y") map (_.asInt32().getValue)


        }
        println("Completed")
        //return observable*/
  }
}
