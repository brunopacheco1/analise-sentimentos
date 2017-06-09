package com.dev.bruno.sentimentanalysis

import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties

import scala.collection.JavaConverters.propertiesAsScalaMapConverter
import scala.io.Source

import org.apache.lucene.analysis.br.RSLPStemmer
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

import spray.json.DefaultJsonProtocol.RootJsObjectFormat
import spray.json.DefaultJsonProtocol.StringJsonFormat
import spray.json.DefaultJsonProtocol.jsonFormat2
import spray.json.JsNull
import spray.json.JsObject
import spray.json.JsString
import spray.json.pimpAny
import spray.json.pimpString
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import spray.json.JsNumber

object TweetProcessor {
  
  private def process(apiAddress : String, stopWords: Broadcast[List[String]], jsonStr: String, model: NaiveBayesModel) {
    case class Model(id: String, text: String)
    implicit val modelFormat = jsonFormat2(Model)
    val json = jsonStr.parseJson.convertTo[Model]

    val polarity = TweetAnalyser.predict(json.text, stopWords, model)

    val tweet = JsObject("id" -> JsString(json.id), "text" -> JsString(json.text), "humanSentiment" -> JsNull, "machineSentiment" -> JsNumber(polarity))

    val newJson = tweet.toJson.prettyPrint

    val url = new URL("http://" + apiAddress + "/tweets/api/tweet")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]

    conn.setDoOutput(true)
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

    val os = conn.getOutputStream
    os.write(newJson.getBytes)
    os.flush
    os.close

		val responseCode = conn.getResponseCode()
		
		println("POST[" + responseCode + "] --> " + json.id)
  }

  def main(args: Array[String]): Unit = {
 
    var apiAddress : String = null
    
    if(args.length == 1) {
      apiAddress = args(0).split("=")(1)
    }
    
    val sparkConf = new SparkConf().setAppName("tweet-processor").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val ssc = new StreamingContext(sc, Seconds(10))
    
    val stopWordsList: List[String] = Source.fromInputStream(getClass().getResourceAsStream("/stopwords.txt")).getLines().map(line => line.trim()).toList
    val stopWords: Broadcast[List[String]] = ssc.sparkContext.broadcast(stopWordsList)

    //CARREGANDO ARQUIVO CSV COM TWEETS SENTIMENTALIZADOS POR PESSOAS - BEGIN
    
    val list = Source.fromURL("http://" + apiAddress + "/tweets/api/tweet/file").mkString.split("\n")
    
    val rdd = sc.parallelize(list)
    
    val rows = rdd.map(line => {
      val split = line.split(";")
      (split(0).toInt, split(1))
    })
    
    val labeledRDD = rows.map(row => LabeledPoint(row._1, TweetAnalyser.transformFeatures(row._2, stopWords)))
    
    labeledRDD.cache()

    val naiveBayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
    
    //CARREGANDO ARQUIVO CSV COM TWEETS SENTIMENTALIZADOS POR PESSOAS - END
    
    val props = new Properties()
    props.load(getClass().getResourceAsStream("/kafka.properties"));

    val topics = Array("tweets-insert")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, props.asScala))

    val kafkaRDDs = stream.map(record => (record.key, record.value))

    kafkaRDDs.foreachRDD(rdd => {
       rdd.foreach(el => process(apiAddress, stopWords, el._2, naiveBayesModel))
    })

    ssc.start()
    ssc.awaitTermination()
  }
}