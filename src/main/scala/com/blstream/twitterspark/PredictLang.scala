package com.blstream.twitterspark

import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }

object PredictLangMain {
  import PredictLang._
  def main(args: Array[String]) {
    predict
  }
}

object PredictLang extends PredictLang with Serializable

trait PredictLang {

  import OAuthConfig._

  def predict = {
    val conf = new SparkConf().setAppName("PredictTweetLangDemo")
    val ssc = new StreamingContext(conf, Seconds(5))

    val tweets = TwitterUtils.createStream(ssc, someAuth)
    val statuses = tweets.map(_.getText)

    val model = new KMeansModel(ssc.sparkContext.objectFile[Vector]("/workspace/twitterspark/model").collect())

    // statuses.foreachRDD(rdd => rdd.foreach(t => println(s">> tweet predicted as ${model.predict(featurize(t))} is: $t")))
    val filteredTweets = statuses.filter(t => model.predict(featurize(t)) == 8)
    filteredTweets.print()

    println("Initialization complete.")
    ssc.start()
    ssc.awaitTermination()
  }

  private def featurize(s: String): Vector = {
    val numFeatures = 1000
    val tf = new HashingTF(numFeatures)
    tf.transform(s.sliding(2).toSeq)
  }

}
