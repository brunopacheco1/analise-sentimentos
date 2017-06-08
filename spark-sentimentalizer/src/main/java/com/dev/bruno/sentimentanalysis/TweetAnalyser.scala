package com.dev.bruno.sentimentanalysis

import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.lucene.analysis.br.RSLPStemmer
import org.apache.spark.broadcast.Broadcast

object TweetAnalyser {

  val hashingTF = new HashingTF()
  
  val stemmer = new RSLPStemmer()

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
  
  def predict(text: String, stopWords: Broadcast[List[String]], model: NaiveBayesModel): Int = {
    model.predict(transformFeatures(text, stopWords)).intValue()
  }
}