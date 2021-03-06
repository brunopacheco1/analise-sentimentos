package com.dev.bruno.sentiments

import java.net.HttpURLConnection
import java.net.URL

import org.apache.lucene.analysis.br.RSLPStemmer
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

import spray.json.DefaultJsonProtocol.StringJsonFormat
import spray.json.DefaultJsonProtocol.jsonFormat5
import spray.json.JsNull
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsString
import spray.json.pimpString

object StatusAnalyzer {

  private val hashingTF = new HashingTF()
  
  private val stemmer = new RSLPStemmer()
  
  private case class Status(id: String, text: String, date: String, source: String, action : String, sentiment: Long)
  
  def process(apiAddress : String, stopWords: Broadcast[List[String]], jsonStr: String, model: NaiveBayesModel) {
    val status = getStatus(stopWords, jsonStr, model)
    
    if(status.action.equals("insert")) {
      insert(apiAddress, status)
    } else {
      update(apiAddress, status)
    }
  }
  
  private def insert(apiAddress : String, status : Status) {
    
    val newJson = JsObject("id" -> JsString(status.id), "text" -> JsString(status.text), "date" -> JsString(status.date), "source" -> JsString(status.source), "humanSentiment" -> JsNull, "machineSentiment" -> JsNumber(status.sentiment)).prettyPrint

    val url = new URL("http://" + apiAddress + "/sentiments/api/status")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]

    conn.setDoOutput(true)
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

    val os = conn.getOutputStream
    os.write(newJson.getBytes)
    os.flush
    os.close

		val responseCode = conn.getResponseCode()
		
		println("INSERT[" + responseCode + "] --> " + status.id + " = " + status.sentiment)
  }
  
  private def update(apiAddress : String, status: Status) {
    
    val url = new URL("http://" + apiAddress + "/sentiments/api/status/" + status.id + "/machineSentiment/" + status.sentiment)
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]

    conn.setDoOutput(true)
    conn.setRequestMethod("PUT")

    val os = conn.getOutputStream
    os.flush
    os.close

		val responseCode = conn.getResponseCode()
		
		println("UPDATE[" + responseCode + "] --> " + status.id + " = " + status.sentiment)
  }
  
  private def getStatus(stopWords: Broadcast[List[String]], jsonStr: String, model: NaiveBayesModel): Status = {
    case class Model(id: String, text: String, date: String, source : String, action : String)
    implicit val modelFormat = jsonFormat5(Model)
    
    val json = jsonStr.parseJson.convertTo[Model]

    val sentiment = model.predict(transformFeatures(json.text, stopWords)).intValue()

    Status(json.id, json.text, json.date, json.source, json.action, sentiment)
  }
  
  def transformFeatures(text: String, stopWords: Broadcast[List[String]]): Vector = {
    hashingTF.transform(getBarebonesText(text, stopWords.value))
  }
  
  private def getBarebonesText(text: String, stopWords: List[String]): Array[String] = {
    text.toLowerCase()
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
      .filter(!stopWords.contains(_)).map(word => stemmer.stem(word))
  }
}