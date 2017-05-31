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

object Sentimentalizer {
  
  private def process(keyFactory: KeyFactory, datastore: Datastore, jsonStr: String) {
    case class Model(id: String, text: String)
    implicit val modelFormat = jsonFormat2(Model)
    val json = jsonStr.parseJson.convertTo[Model]

    println(jsonStr)
    //INCLUIR AQUI ANALISE DE SENTIMENTOS
    /*val key = keyFactory.newKey(json.id);
    
    val entity = datastore.get(key, ReadOption.eventualConsistency());
    
    val builder: Entity.Builder = Entity.newBuilder(entity);

		builder.set("machineSentiment", 1);

		val newEntity = builder.build();

		datastore.update(newEntity);*/
  }

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("sentimentalizer").setMaster("local[*]")

    val warehouseLocation = "file:${system:user.dir}/spark-warehouse"

    val spark = SparkSession
      .builder
      .config(sparkConf)
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .enableHiveSupport()
      .getOrCreate()

    spark.sql("DROP TABLE IF EXISTS tweet")
    spark.sql("create table tweet(id STRING, text STRING, sentiment long)")

    val sc = spark.sparkContext

    val datastore = DatastoreOptions.newBuilder().setProjectId("sentimentalizer-169016").setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("/opt/credentials/sentimentalizer.json"))).build().getService()

    val keyFactory = datastore.newKeyFactory().setKind("tweet")

    val query: Query[Entity] = Query.newEntityQueryBuilder().setKind("tweet").setFilter(CompositeFilter.and(PropertyFilter.ge("humanSentiment", -1), PropertyFilter.le("humanSentiment", 1))).build()

    val result: QueryResults[Entity] = datastore.run(query, ReadOption.eventualConsistency())

    var tweets = new ListBuffer[Row]()

    while (result.hasNext()) {
      val el = result.next()

      if (!el.isNull("humanSentiment")) {
        tweets += Row(el.getString("id"), el.getString("text"), el.getLong("humanSentiment"))
      }
    }

    val schema = StructType(Array[StructField](StructField("id", StringType, nullable = false), StructField("text", StringType, nullable = false), StructField("sentiment", LongType, nullable = false)))

    val tweetsDF = spark.createDataFrame(sc.parallelize(tweets), schema)

    tweetsDF.createOrReplaceTempView("temp_tweet")

    spark.sql("INSERT INTO TABLE tweet SELECT * FROM temp_tweet")

    val consulta = spark.sql("SELECT * FROM tweet where sentiment is not null")
    
    //INCLUIR AQUI TREINAMENTO DA INTELIGENCIA ARTIFICIAL

    val ssc = new StreamingContext(sc, Seconds(10))
    
    val props = new Properties()
		props.load(new FileInputStream("/opt/credentials/kafka.properties"));

    val topics = Array("tweets-evaluation")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, props.asScala))

    val streamMap = stream.map(record => (record.key, record.value))

    streamMap.foreachRDD(rdd => {
      val resultStreamMap = rdd.collect().foreach(el => process(keyFactory, datastore, el._2))
    })

    ssc.start()
    ssc.awaitTermination()

  }
}