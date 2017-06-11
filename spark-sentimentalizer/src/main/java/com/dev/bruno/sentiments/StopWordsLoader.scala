package com.dev.bruno.sentiments

import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.lucene.analysis.br.RSLPStemmer
import org.apache.spark.broadcast.Broadcast

import spray.json.DefaultJsonProtocol._
import spray.json._

import java.net.HttpURLConnection
import java.net.URL
import scala.io.Source
import org.apache.spark.SparkContext

object StopWordsLoader {

  def load(sc: SparkContext): Broadcast[List[String]] = {
    sc.broadcast(Source.fromInputStream(getClass().getResourceAsStream("/stopwords.txt")).getLines().map(line => line.trim()).toList)
  }
}