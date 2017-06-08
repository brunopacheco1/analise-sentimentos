package com.dev.bruno.sentimentanalysis

import org.apache.spark.SparkConf
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.QueryResults
import com.google.cloud.datastore.StructuredQuery.CompositeFilter
import com.google.auth.oauth2.ServiceAccountCredentials
import java.io.FileInputStream
import com.google.cloud.datastore.StructuredQuery.PropertyFilter
import com.google.cloud.datastore.ReadOption
import scala.collection.mutable.ListBuffer
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
    
    if(args.length == 1) {
      naivesBayesModelPath = args(0).split("=")(1)
    }
    
    val stopWordsList: List[String] = Source.fromInputStream(getClass().getResourceAsStream("/stopwords.txt")).getLines().map(line => line.trim()).toList
    val stopWords: Broadcast[List[String]] = sc.broadcast(stopWordsList)
    
    val datastore = DatastoreOptions.newBuilder().setProjectId("sentimentalizer-169016").setCredentials(ServiceAccountCredentials.fromStream(getClass().getResourceAsStream("/sentimentalizer.json"))).build().getService()

    val keyFactory = datastore.newKeyFactory().setKind("tweet")

    val query: Query[Entity] = Query.newEntityQueryBuilder().setKind("tweet").setFilter(CompositeFilter.and(PropertyFilter.ge("humanSentiment", 0), PropertyFilter.le("humanSentiment", 4))).build()

    val result: QueryResults[Entity] = datastore.run(query, ReadOption.eventualConsistency())

    var tweets = new ListBuffer[Row]()

    while (result.hasNext()) {
      val el = result.next()
      
      if (!el.isNull("humanSentiment")) {
        tweets += Row(el.getString("text"), el.getLong("humanSentiment").intValue())
      }
    }

    val schema = StructType(Array[StructField](StructField("text", StringType, nullable = false), StructField("sentiment", IntegerType, nullable = false)))

    val tweetsDF = spark.createDataFrame(sc.parallelize(tweets), schema)

    val labeledRDD = tweetsDF.select("sentiment", "text").rdd.map {
      case Row(sentiment: Int, text: String) => LabeledPoint(sentiment, TweetAnalyser.transformFeatures(text, stopWords))
    }
    
    labeledRDD.cache()

    val naiveBayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
    
    naiveBayesModel.save(spark.sparkContext, naivesBayesModelPath)
  }   
}