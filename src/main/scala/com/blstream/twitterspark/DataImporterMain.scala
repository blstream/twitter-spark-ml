package com.blstream.twitterspark

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.{ SparkConf, SparkContext }

import com.google.gson.Gson

object DataImporterMain {
  import DataImporter._

  def main(args: Array[String]) {
    fetchTweets()
  }

}

object DataImporter extends DataImporter

trait DataImporter {
  import FetchConfig._
  import OAuthConfig._

  private var numTweetsCollected = 0L
  private var partNum = 0

  private def prepareStreamingContext = {
    val conf = new SparkConf().setAppName("TweetImportDemo")
    val sc = new SparkContext(conf)
    new StreamingContext(sc, Seconds(intervalSecs))
  }

  def fetchTweets(): Unit = {
    val ssc = prepareStreamingContext

    val tweetStream = TwitterUtils.createStream(ssc, someAuth).map(new Gson().toJson(_))
    saveTweets(tweetStream)

    ssc.start()
    ssc.awaitTermination()
  }

  private def saveTweets(tweetStream: DStream[String]) = {
    tweetStream.foreachRDD((rdd, time) => {
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(partitionsEachInterval)
        outputRDD.saveAsTextFile(outputDirectory + "/tweets_" + time.milliseconds.toString)
        numTweetsCollected += count
        if (numTweetsCollected > numTweetsToCollect) {
          System.exit(0)
        }
      }
    })
  }

}
