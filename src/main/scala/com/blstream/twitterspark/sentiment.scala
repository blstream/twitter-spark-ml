package com.blstream.twitterspark

import java.util.Properties
import Predef.{ any2stringadd => _, StringAdd => _, _ }
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.CoreNLPProtos.Sentiment
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }

import scala.collection.JavaConversions._

object SentimentAnalysisMain extends SentimentAnalysis with Serializable {
  def main(args: Array[String]) {
    import org.apache.log4j.Logger
    import org.apache.log4j.Level

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
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

    englishTweets.map(s => predictSentiment(s.getText)).print()

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
    val ratedSentences = for (s <- ann.get(classOf[CoreAnnotations.SentencesAnnotation])) yield {
      val tree = s.get(classOf[SentimentAnnotatedTree])

      RatedSentence(s.toString, Option(Sentiment.valueOf(RNNCoreAnnotations.getPredictedClass(tree))))
    }

    val (ws, rs) = ratedSentences.foldLeft((0, 0.0)) {
      case ((charSum, ratingSum), RatedSentence(sentenceText, Some(sentiment))) =>
        (charSum + sentenceText.length, ratingSum + sentiment.getNumber * sentenceText.length)
      case (e, _) => e
    }

    val avgSentiment = rs / ws

    if (avgSentiment.isInfinite || avgSentiment.isNaN) RatedTweet(text, None)
    else RatedTweet(text, Option(Sentiment.valueOf(avgSentiment.round.toInt)))

  }

}

case class RatedSentence(text: String, sentiment: Option[Sentiment])
case class RatedTweet(text: String, sentiment: Option[Sentiment])