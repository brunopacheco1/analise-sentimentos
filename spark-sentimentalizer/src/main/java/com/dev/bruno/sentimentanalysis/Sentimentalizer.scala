package com.dev.bruno.sentimentanalysis

import java.security.MessageDigest

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.elasticsearch.spark.rdd.EsSpark

import spray.json._
import DefaultJsonProtocol._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds

import com.amazonaws.auth.{ BasicAWSCredentials, DefaultAWSCredentialsProviderChain }
import com.amazonaws.regions.RegionUtils
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.model.PutRecordRequest
import org.apache.spark.streaming.kinesis.KinesisUtils
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Milliseconds

object Sentimentalizer {

  def main(args: Array[String]): Unit = {
    
    val sparkConf = new SparkConf().setAppName("Sentimentalizer").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val ssc = new StreamingContext(sc, Seconds(2))

    val credentials = new DefaultAWSCredentialsProviderChain().getCredentials()

    val kinesisStreams = (0 until 1).map { i =>
      KinesisUtils.createStream(ssc, "bab-search-engine-consumer", "bab-search-engine", "kinesis.us-east-1.amazonaws.com", "us-east-1",
        InitialPositionInStream.LATEST, Milliseconds(2000), StorageLevel.MEMORY_ONLY)
    }

    val unionStreams = ssc.union(kinesisStreams)
    unionStreams.foreachRDD(rdd => {
      val result = rdd.map(document => buildResult(new String(document)))

      EsSpark.saveToEs(result, "documents/document", Map("es.mapping.id" -> "id"))
    })

    ssc.start()
    ssc.awaitTermination()
  }
}