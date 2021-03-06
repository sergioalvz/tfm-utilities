#!/bin/sh
exec scala "$0" "$@"
!#
import scala.io.Source
import scala.collection.mutable._
import scala.xml.pull._
import scala.xml._

import java.io._

/*******************************************************************************
 *******************************************************************************
 *                              FUNCTIONS                                      *
 *******************************************************************************
 *******************************************************************************/

def getBoundingBoxes(root: Elem): List[List[(Double, Double)]] = {
  var boundingBoxes = new ListBuffer[List[(Double, Double)]]
  (root \\ "boundingBox").foreach(b => {
    val sw_long = (b \ "sw" \ "longitude").text
    val sw_lat = (b \ "sw" \ "latitude").text
    val ne_long = (b \ "ne" \ "longitude").text
    val ne_lat = (b \ "ne" \ "latitude").text

    boundingBoxes += List((sw_lat.toDouble, sw_long.toDouble), (ne_lat.toDouble, ne_long.toDouble))
  })
  boundingBoxes.toList
}

def isInBoundingBoxes(latitude: String, longitude: String): Boolean = {
  boundingBoxes.foreach(b => {
    val sw = b(0)
    val ne = b(1)
    if(sw._1 <= latitude.toDouble && ne._1 >= latitude.toDouble && sw._2 <= longitude.toDouble && ne._2 >= longitude.toDouble) return true
  })
  false
}

def isIncludable(latitude: String, longitude: String): Boolean = {
  if(mustBeIn) isInBoundingBoxes(latitude, longitude) else !isInBoundingBoxes(latitude, longitude)
}

def buildTweet(values: List[String]): String = {
  val tweet =
  <tweet>
    <id>{values(0)}</id>
    <username>{values(1)}</username>
    <name>{values(2)}</name>
    <location>{values(3)}</location>
    <timezone>{values(4)}</timezone>
    <createdAt>{values(5)}</createdAt>
    <latitude>{values(6)}</latitude>
    <longitude>{values(7)}</longitude>
    <text>{values(8)}</text>
  </tweet>

  s"\n${tweet.toString}\n"
}

def serialize(tweet: String) = { writer.write(tweet) }

// id, username, name, location, timezone, createdAt, latitude, longitude, text
def save(values: List[String]) = {
  val latitude  = values(6)
  val longitude = values(7)
  if(isIncludable(latitude, longitude)){
    serialize(buildTweet(values))
  }
}

def appendToLastValue(text:String, values:ListBuffer[String]): Unit = {
  var last = values(values.size - 1)
  values(values.size - 1) = last + text;
}

def getWriter(): BufferedWriter = {
  val suffix = if(mustBeIn) "_in_bounding_boxes" else "_no_bounding_boxes"
  val out = new File(tweetsFile + "_tweets_by_location" + suffix + ".xml")
  if(!out.exists) out.createNewFile

  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out)))
}

/*******************************************************************************
 *******************************************************************************
 *                              SCRIPT STARTS                                  *
 *******************************************************************************
 *******************************************************************************/

val tweetsFile        = args(0).toString
val boundingBoxesFile = args(1).toString
val mustBeIn          = args(2).toBoolean

val boundingBoxes = getBoundingBoxes(XML.loadFile(boundingBoxesFile))

val tweets = new XMLEventReader(Source.fromFile(tweetsFile))
var values = new ListBuffer[String]

val writer: BufferedWriter = getWriter()

try {
  var current = ""
  var last = ""

  writer.write("<tweets>")
  tweets.foreach(event => {
    event match {
      case e: EvElemStart if e.label != "tweets" && e.label != "tweet" =>
      current = e.label
      case e: EvElemEnd if e.label != "tweets" && e.label != "tweet" =>
      current = ""
      case EvElemEnd(_, "tweet") =>
      save(values.toList)
      values.clear
      case EvText(text) if Array("id", "username", "name", "location", "latitude", "longitude", "timezone", "createdAt", "text").contains(current) =>
      if(current == last) appendToLastValue(text, values) else values += text
      last = current
      case EvEntityRef("amp") =>
      if(current == last) appendToLastValue("&", values) else values += "&"
      last = current
      case EvEntityRef("lt")  =>
      if(current == last) appendToLastValue("<", values) else values += "<"
      last = current
      case EvEntityRef("gt")  =>
      if(current == last) appendToLastValue(">", values) else values += ">"
      last = current
      case _ => ;
    }
  })
  writer.write("</tweets>")
} finally {
  writer.flush()
  writer.close()
}
