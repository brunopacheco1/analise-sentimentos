package com.dev.bruno.sentiments

import scala.io.Source

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast

object StopWordsLoader {

  def load(sc: SparkContext): Broadcast[List[String]] = {
    sc.broadcast(Source.fromInputStream(getClass().getResourceAsStream("/stopwords.txt")).getLines().map(line => line.trim()).toList)
  }
}