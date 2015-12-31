package com.blstream.twitterspark

import com.basho.riak.client.api.RiakClient
import com.basho.riak.client.api.commands.datatypes.{ UpdateSet, SetUpdate }
import com.basho.riak.client.core.query.{ Location, Namespace }
import com.basho.riak.client.core.util.BinaryValue
import com.google.gson.Gson
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.{ SparkConf, SparkContext }

import org.apache.spark.rdd.RDD
import com.basho.riak.spark.rdd._
import org.apache.spark.{ SparkContext, SparkConf }
import com.basho.riak.spark._
import com.basho.riak.spark.writer.{ WriteDataMapper, WriteDataMapperFactory }

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
    val conf = new SparkConf()
      .setAppName("TweetImportDemo")
      .set("spark.riak.connection.host", "localhost:8087")
      .set("spark.riak.connections.min", "20")
      .set("spark.riak.connections.max", "50")
    val sc = new SparkContext(conf)
    new StreamingContext(sc, Seconds(intervalSecs))
  }

  def fetchTweets(): Unit = {
    val ssc = prepareStreamingContext

    val tweetStream = TwitterUtils.createStream(ssc, someAuth).map(x => (x.getId, new Gson().toJson(x)))
    saveToRiak(tweetStream)

    ssc.start()
    ssc.awaitTermination()
  }

  val bucketType = new Namespace("tweet-sets", "test5")

  def saveToRiak(tweetStream: DStream[(Long, String)]) =
    tweetStream.foreachRDD { (rdd, time) =>
      val count = rdd.count()
      if (count > 0) {
        rdd.saveToRiak(bucketType)
        val ids = rdd.map { case (x, _) => x }
        saveKeysToRiakSet(ids.collect())
        numTweetsCollected += count
        if (numTweetsCollected > numTweetsToCollect) {
          System.exit(0)
        }
      }
    }

  private def saveKeysToRiakSet: Array[Long] => Unit =
    ids => {
      val client = RiakClient.newClient("localhost")
      val su = new SetUpdate()
      ids.foreach(id => su.add(id.toString))
      val tweetsSet = new Location(new Namespace("tweet-sets", "test5"), "tweets-keys")
      val update = new UpdateSet.Builder(tweetsSet, su).build()
      client.execute(update)
      client.shutdown()
    }

}
