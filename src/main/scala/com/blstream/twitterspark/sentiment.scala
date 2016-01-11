package com.blstream.twitterspark

import org.apache.spark.{ SparkContext, SparkConf }
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import twitter4j.Status

object SentimentAnalysisMain extends SentimentAnalysis with Serializable {
  def main(args: Array[String]) {
    analyzeTweets()
  }
}

trait SentimentAnalysis {
  import OAuthConfig._

  def analyzeTweets() = {
    val conf = new SparkConf().setAppName("SentimentTweetDemo")
    val ssc = new StreamingContext(conf, Seconds(5))

    val codec = scala.io.Codec("utf8")

    val positiveWords = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/positive-words.txt"), "UTF-8").getLines().filterNot(_.startsWith(";")).toSet
    val negativeWords = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/negative-words.txt"), "ISO-8859-1").getLines().filterNot(_.startsWith(";")).toSet

    val tweets = TwitterUtils.createStream(ssc, someAuth)
    val englishTweets = tweets.filter(_.getUser.getLang == "en")

    val rated = englishTweets.map { s =>
      RatedTweet(positiveWords.intersect(wordSet(s)), negativeWords.intersect(wordSet(s)), s.getText)
    }

    rated.filter(r => r.positive.nonEmpty || r.negative.nonEmpty).map(r => s"${r.positive.size}\t${r.negative.size}\t${r.tweet}").print()

    ssc.start()
    ssc.awaitTermination()
  }

  def wordSet(s: Status): Set[String] = {
    s.getText.split("\\s+").map(_.toLowerCase).toSet
  }
}

case class RatedTweet(positive: Set[String], negative: Set[String], tweet: String)