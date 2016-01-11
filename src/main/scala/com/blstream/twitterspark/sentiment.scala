package com.blstream.twitterspark

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }

import scala.collection.JavaConversions._

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

    val tweets = TwitterUtils.createStream(ssc, someAuth)
    val englishTweets = tweets.filter(_.getUser.getLang == "en")

    englishTweets.flatMap(s => predictSentiment(s.getText)).print()

    ssc.start()
    ssc.awaitTermination()
  }

  val nlpProps = {
    val p = new Properties()
    p.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    p
  }

  def predictSentiment = { text: String =>

    val pipeline = new StanfordCoreNLP(nlpProps)
    val ann = pipeline.process(text)
    for (s <- ann.get(classOf[CoreAnnotations.SentencesAnnotation])) yield {
      val tree = s.get(classOf[SentimentAnnotatedTree])

      (s, RNNCoreAnnotations.getPredictedClass(tree))
    }
  }

}

case class RatedTweet(positive: Set[String], negative: Set[String], tweet: String)