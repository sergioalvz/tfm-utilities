#!/bin/sh
exec scala "$0" "$@"
!#
import java.io._
import scala.io.Source

/* ==================================================
 *                   METHODS
 * ================================================== */

def writeLine(line:String, file:String):Unit = {
  val out = new File(file)
  if(!out.exists) out.createNewFile
  val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out, true)))
  try {
    writer.write(line)
  } finally {
    writer.flush()
    writer.close()
  }
}

def getClass(score:Double):Int = {
  if(score < 0.0) 0 else 1
}

/* ==================================================
 *                   SCRIPT
 * ================================================== */
val tsv = args(0)
val outputFileName = s"${tsv}_vw"
Source.fromFile(tsv).getLines.foreach(line => {
  val columns = line.split("\t")
  val (classValue, tweet) = (getClass(columns(0).toDouble), columns(3))
  val resultLine = s"$classValue |Tweet $tweet\n"
  writeLine(resultLine, outputFileName)
})
