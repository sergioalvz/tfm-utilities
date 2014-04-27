#!/bin/sh
exec scala "$0" "$@"
!#
import scala.io.Source
import scala.xml.pull._
import scala.xml.pull.EvElemStart
import scala.xml.pull.EvText
import java.io._


val fileToAnalyze = args(0).toString
var freq = collection.mutable.Map[String, Int]().withDefaultValue(0)
var isCreatedAtElement = false

new XMLEventReader(Source.fromFile(fileToAnalyze)).foreach(event => {
  event match {
    case EvElemStart(_, "createdAt", _, _) => isCreatedAtElement = true
    case EvText(text) if isCreatedAtElement => {
      val hour = text.trim.slice(text.trim.lastIndexOf(" "), text.trim.length - 1)
      freq(hour) += 1
    }
    case EvElemEnd(_, "createdAt") => isCreatedAtElement = false
    case _ => ;
  }
})

val sorted = freq.toList.sortBy({ _._1})

val file = new File(s"${fileToAnalyze}_tweetsByTime.tsv")
if(!file.exists) file.createNewFile

val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))
try{
  sorted.foreach(t => writer.write(s"${t._2}\t${t._1}0\n"))
}finally{
  writer.flush()
  writer.close()
}
