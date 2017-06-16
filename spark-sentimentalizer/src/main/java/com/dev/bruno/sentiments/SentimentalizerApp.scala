package com.dev.bruno.sentiments

import java.util.Properties

import scala.collection.JavaConverters.propertiesAsScalaMapConverter

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

object SentimentalizerApp {
  
  def main(args: Array[String]): Unit = {
 
    var apiAddress : String = null
    
    if(args.length == 1) {
      apiAddress = args(0).split("=")(1)
    }
    
    val sparkConf = new SparkConf().setAppName("SentimentalizerApp").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    //CARREGANDO STOPWORDS
    val stopWords = StopWordsLoader.load(sc)

    //CARREGANDO MODELO NAIVE BAYES
    val naiveBayesModel = NaiveBayesLoader.load(apiAddress, sc, stopWords)
    
    //INICIANDO CONTEXT STREAM
    val ssc = new StreamingContext(sc, Seconds(10))
    
    val props = new Properties()
    props.load(getClass().getResourceAsStream("/kafka.properties"));

    val topics = Array("status-process")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, props.asScala))

    val kafkaRDDs = stream.map(record => (record.key, record.value))

    //PROCESSANDO STATUS DO KAFKA
    kafkaRDDs.foreachRDD(rdd => {
      rdd.foreach(el => {
        StatusAnalyzer.process(apiAddress, stopWords, el._2, naiveBayesModel)
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}