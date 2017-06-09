package com.dev.bruno.sentimentanalysis

import org.apache.spark.SparkConf
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.IntegerType
import scala.io.Source
import org.apache.spark.broadcast.Broadcast

object CreateNaiveBayesModel {
  
  def main(args: Array[String]): Unit = {
    
    val sparkConf = new SparkConf().setAppName("CreateNaiveBayesModel").setMaster("local[*]")

    val spark = SparkSession
      .builder
      .config(sparkConf)
      .getOrCreate()
      
    val sc = spark.sparkContext
    
    var naivesBayesModelPath : String = null
    var sentimentDatasetPath : String = null
    
    if(args.length == 2) {
      naivesBayesModelPath = args(0).split("=")(1)
      sentimentDatasetPath = args(1).split("=")(1)
    }
    
    val stopWordsList: List[String] = Source.fromInputStream(getClass().getResourceAsStream("/stopwords.txt")).getLines().map(line => line.trim()).toList
    val stopWords: Broadcast[List[String]] = sc.broadcast(stopWordsList)
    
    val tweetsDF = spark.read
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(sentimentDatasetPath)
      .toDF("sentiment", "text")

    val labeledRDD = tweetsDF.select("sentiment", "text").rdd.map {
      case Row(sentiment: Int, text: String) => LabeledPoint(sentiment, TweetAnalyser.transformFeatures(text, stopWords))
    }
    
    labeledRDD.cache()

    val naiveBayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
    
    naiveBayesModel.save(spark.sparkContext, naivesBayesModelPath)
  }   
}