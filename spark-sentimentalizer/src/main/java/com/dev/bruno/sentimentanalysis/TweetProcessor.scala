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

object TweetProcessor {

  def getBarebonesTweetText(tweetText: String, stopWordsList: List[String]): String = {
    //Remove URLs, RT, MT and other redundant chars / strings from the tweets.

    val stemmer = new RSLPStemmer()

    tweetText.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("\\s+#\\w+", "")
      .replaceAll("#\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "").replaceAll("[^a-zA-Z\\sàÀáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛãẽĩõũÃẼĨÕŨçÇüÜ]", "")
      .split("\\s+")
      .filter(_.matches("^[a-zA-ZàÀáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛãẽĩõũÃẼĨÕŨçÇüÜ]+$"))
      .filter(!stopWordsList.contains(_)).map(word => stemmer.stem(word)).mkString(" ")
  }

  private def process(apiAddress : String, stopWords: Broadcast[List[String]], jsonStr: String) {
    case class Model(id: String, text: String)
    implicit val modelFormat = jsonFormat2(Model)
    val json = jsonStr.parseJson.convertTo[Model]

    val cleanText = getBarebonesTweetText(json.text, stopWords.value)

    val tweet = JsObject("id" -> JsString(json.id), "text" -> JsString(json.text), "cleanText" -> JsString(cleanText), "humanSentiment" -> JsNull, "machineSentiment" -> JsNull)

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

    val list: List[String] = Source.fromInputStream(getClass().getResourceAsStream("/stopwords.txt")).getLines().map(line => line.trim()).toList

    val stopWords: Broadcast[List[String]] = ssc.sparkContext.broadcast(list)

    val props = new Properties()
    props.load(getClass().getResourceAsStream("/kafka.properties"));

    val topics = Array("tweets-insert")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, props.asScala))

    val streamMap = stream.map(record => (record.key, record.value))

    streamMap.foreachRDD(rdd => {
      val resultStreamMap = rdd.collect().foreach(el => process(apiAddress, stopWords, el._2))
    })

    ssc.start()
    ssc.awaitTermination()
  }
}