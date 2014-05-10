#!/bin/sh
exec scala "$0" "$@"
!#
import java.io._
import scala.io.Source
import scala.collection.mutable.ListBuffer

val TRAINING_PERCENT = 0.8

val dataset = args(0)
val lines = Source.fromFile(dataset).getLines.toArray

val (trainingUsers, testingUsers) = divideUsersForDatasets(lines)

writeLines(dataset + "_train", trainingUsers, lines)
writeLines(dataset + "_test", testingUsers, lines)

/* ==================================================
 *                   FUNCTIONS
 * ================================================== */

def divideUsersForDatasets(lines:Array[String]):(Array[String], Array[String]) = {
  val users = new ListBuffer[String]
  lines.foreach(line => {
      val username = getUserByLine(line)
      if(!users.contains(username)) users += username
  })

  val forTraining = (users.length * TRAINING_PERCENT).toInt
  val train = users.slice(0, forTraining)
  val test  = users.slice(forTraining, users.length)

  (train.toArray, test.toArray)
}

def writeLines(datasetName:String, users:Array[String], lines:Array[String]):Unit = {
  val out = new File(datasetName)
  if(!out.exists) out.createNewFile
  val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out, true)))
  try {
    lines.foreach(line => if(users.contains(getUserByLine(line))) writer.write(line + "\n"))
  } finally {
    writer.flush()
    writer.close()
  }
}

def getUserByLine(line:String):String = {
  val columns = line.split("\t");
  val fields = columns(3).split(" ")
  fields(0)
}
