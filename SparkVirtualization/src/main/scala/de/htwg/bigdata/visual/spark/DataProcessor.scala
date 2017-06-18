package de.htwg.bigdata.visual.spark

import com.mongodb.spark.MongoSpark

import com.mongodb.spark.config.ReadConfig
import com.typesafe.config.ConfigFactory
import org.bson.Document
import com.mongodb.spark.rdd.MongoRDD
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import de.htwg.bigdata.visual.resource.GridRequest
import scala.math.Ordering
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import com.fasterxml.jackson._
import scala.xml.Group
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.io.StringWriter

class DataProcessor extends java.io.Serializable {
  case class GridRepresentation(step: Int, time: Long, fields: Array[Document])
  
  def transformGrid(gridRequest: GridRequest): String = {

    Logger.getRootLogger.setLevel(Level.ERROR)
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val sc = new SparkContext("local[*]", "DataProcessor")
    val config = ConfigFactory.load()
    
    // val rdd = loadRDD(gridRequest)
    val readConfig = new ReadConfig(config.getString("mongodb.db"), gridRequest.collection, Some(config.getString("mongodb.uri")))
    val rdd = MongoSpark.load(sc, readConfig)
    println("loaded RDD from MongoDB")
    val columns = extractConfig(rdd)
    val antsPos = extractAntPos(rdd)
    println("extracted Columns and Ant positions")
    //(3,123)
    //(3,222)  <---
    val antNumber = antsPos.map(doc => (doc.getString("id").toInt, doc.getLong("timestamp").toInt))
      .map(item => item.swap).sortByKey(false, 1)
      .map(item => item.swap)
      .reduceByKey((a: Int, b: Int) => a).count()

    println("Calculated Ant Count")

    val ratio = columns.first.getInteger("rows").toFloat / gridRequest.x.toFloat

    println("Ratio")

    val firstTimestamp = antsPos.min()(TimeOrdering).getLong("timestamp")
    val lastTimestamp = antsPos.max()(TimeOrdering).getLong("timestamp")
    var currentMillis = 0

    println("Calculated First and Last Timestamp")

    var gridRepresentation = List[GridRepresentation]()
    var stepCount = 1

    var cumulativeAnts: RDD[Document] = null
    do {
      //current Pos for each timestep
      val currentPos = antsPos.filter(doc => filterCurrentPos(gridRequest, doc, currentMillis))
      println("STEP" + stepCount + " Calculated Ant Count. CurrentPos:")

      //      currentPos.foreach { f => println(f) }

      if (cumulativeAnts == null) cumulativeAnts = currentPos else cumulativeAnts = sc.union(cumulativeAnts, currentPos)

      // sort by timestamp, reduce by id
      //(3,123)
      //(3,222)  <---
      cumulativeAnts = cumulativeAnts
        .map(doc => (doc.getLong("timestamp").toInt, doc))
        .sortByKey(false, 1)
        .map(t => ((t._2.getString("id").toInt, t._2)))
        .reduceByKey((a: Document, b: Document) => a)
        .map(t => t._2)

      println("STEP" + stepCount + " Calculated current Ants. CumANts:")
      //       cumulativeAnts.foreach { f => println(f) }

      val currentAnts = cumulativeAnts
      //currentAnts.foreach(doc => println("current Ants:" + doc))

      //calculate new x and y
      // newX=x*ratio
      val transormedGrid = currentAnts.map(t =>
        {
          var doc = t
          doc.append("newX", (Math.min((doc.getInteger("x") / ratio), gridRequest.x - 1)).toInt)
          doc.append("newY", (Math.min((doc.getInteger("y") / ratio), gridRequest.x - 1)).toInt)
          doc.append("posID", (doc.get("newX").toString() + "_" + doc.get("newY").toString()))
        }).map(doc => (doc.get("posID").toString().hashCode(), doc))
      //.reduceByKey((doc: Document, doc2: Document) => doc)
      println("STEP" + stepCount + " Calculated transformed Grid")

      //(id,count)
      val antCount = transormedGrid.countByKey()
      println("STEP" + stepCount + " Calculated Ant Count")

      //calculate concentration
      val newGrid = transormedGrid.map(doc => {
        doc._2.append("ants", (antCount.get(doc._1)) match {
          case Some(x: Long) => x // this extracts the value in a as an Int
          case _ => 0L
        })
        var conc = doc._2.getLong("ants").toFloat / antNumber * 100
        doc._2.append("concentration", conc)
      })

      println("STEP" + stepCount + " Calculated concentration")

      //build json
      val step = GridRepresentation(stepCount, currentMillis, newGrid.collect())
      gridRepresentation ::= step
      println("STEP" + stepCount + " added in List")

      currentMillis += gridRequest.timestep
      stepCount = stepCount + 1
    } while (currentMillis <= lastTimestamp)
    println("Finished Calculation, starting tp parse....")

    return parseToJson(gridRepresentation)
  }

  private def loadRDD(gridRequest: GridRequest): MongoRDD[Document] = {
    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "DataProcessor")
    val config = ConfigFactory.load()
    val readConfig = new ReadConfig(config.getString("mongodb.db"), gridRequest.collection, Some(config.getString("mongodb.uri")))
    return MongoSpark.load(sc, readConfig)
  }

  private def parseToJson(gridRep: List[GridRepresentation]): String = {

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val out = new StringWriter
    mapper.writeValue(out, gridRep)

    val json = out.toString()
    println("finished parsing")
    return json
  }

  private def extractConfig(rdd: MongoRDD[Document]) = rdd.filter(doc => doc.containsKey("rows"))
  private def extractAntPos(rdd: MongoRDD[Document]) = rdd.filter(doc => doc.containsKey("x"))
  private def filterCurrentPos(gridRequest: GridRequest, doc: Document, currentMillis: Long): Boolean = if (currentMillis - gridRequest.timestep < doc.getLong("timestamp") && doc.getLong("timestamp") < currentMillis) true else false

  object TimeOrdering extends Ordering[Document] {
    def compare(doca: Document, docb: Document) = doca.getLong("timestamp").toInt compare docb.getLong("timestamp").toInt
  }
}