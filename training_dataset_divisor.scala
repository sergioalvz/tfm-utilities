#!/bin/sh
exec scala "$0" "$@"
!#
import java.io._
import scala.io.Source

val TRAINING_PERCENT = 0.8

val dataset = args(0)
val lines = Source.fromFile(dataset).getLines.toArray
val forTraining = (lines.length * TRAINING_PERCENT).toInt
val train = lines.slice(0, forTraining)
val test  = lines.slice(forTraining, lines.length)

writeLines(dataset + "_train", train)
writeLines(dataset + "_test", test)

def writeLines(datasetName:String, lines:Array[String]):Unit = {
  val out = new File(datasetName)
  if(!out.exists) out.createNewFile
  val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out, true)))
  try {
    lines.foreach(line => writer.write(line + "\n"))
  } finally {
    writer.flush()
    writer.close()
  }
}
