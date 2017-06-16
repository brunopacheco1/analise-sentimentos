package com.dev.bruno.sentiments

import scala.io.Source

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.regression.LabeledPoint

object NaiveBayesLoader {

  def load(apiAddress : String, sc : SparkContext, stopWords: Broadcast[List[String]]): NaiveBayesModel = {
    val list = Source.fromURL("http://" + apiAddress + "/sentiments/api/status/sentimentalized/csv").mkString.split("\n")
    
    val rdd = sc.parallelize(list)
    
    val rows = rdd.map(line => {
      val split = line.split(";")
      (split(0).toInt, split(1))
    })
    
    val labeledRDD = rows.map(row => LabeledPoint(row._1, StatusAnalyzer.transformFeatures(row._2, stopWords)))
    
    labeledRDD.cache()

    NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
  }
}