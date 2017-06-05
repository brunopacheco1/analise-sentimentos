package com.dev.bruno.sentimentanalysis

import java.security.MessageDigest
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import com.google.cloud.datastore.DatastoreOptions
import java.io.FileInputStream
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.StructuredQuery.OrderBy
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.QueryResults
import com.google.cloud.datastore.ReadOption
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.Row
import com.google.cloud.datastore.StructuredQuery.PropertyFilter
import com.google.cloud.datastore.StructuredQuery.CompositeFilter
import scala.collection.mutable.ListBuffer
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import spray.json._
import DefaultJsonProtocol._
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.KeyFactory
import scala.collection.JavaConverters._
import java.util.Properties
import org.apache.lucene.analysis.br.RSLPStemmer
import scala.io.Source
import org.apache.spark.broadcast.Broadcast

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
    //.fold("")((a,b) => a.trim + " " + b.trim).trim
  }

  private def process(keyFactory: KeyFactory, datastore: Datastore, stopWords: Broadcast[List[String]], jsonStr: String) {
    case class Model(id: String, text: String)
    implicit val modelFormat = jsonFormat2(Model)
    val json = jsonStr.parseJson.convertTo[Model]

    val key = keyFactory.newKey(json.id);

    val builder: Entity.Builder = Entity.newBuilder(key)

    builder.set("id", json.id)

    builder.set("text", json.text)

    builder.setNull("machineSentiment")

    builder.setNull("humanSentiment")

    val cleanText = getBarebonesTweetText(json.text, stopWords.value)

    println(json.text + " --> " + cleanText);

    builder.set("cleanText", cleanText)

    val newEntity = builder.build()

    datastore.add(newEntity)
  }

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("tweet-processor").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val datastore = DatastoreOptions.newBuilder().setProjectId("sentimentalizer-169016").setCredentials(ServiceAccountCredentials.fromStream(getClass().getResourceAsStream("/sentimentalizer.json"))).build().getService()

    val keyFactory = datastore.newKeyFactory().setKind("tweet")

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
      val resultStreamMap = rdd.collect().foreach(el => process(keyFactory, datastore, stopWords, el._2))
    })

    ssc.start()
    ssc.awaitTermination()
  }
}