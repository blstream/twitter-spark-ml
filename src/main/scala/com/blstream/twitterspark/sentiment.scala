package com.blstream.twitterspark

import java.util.Properties
import edu.stanford.nlp.util.logging.RedwoodConfiguration

import Predef.{ any2stringadd => _, StringAdd => _, _ }
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.CoreNLPProtos.Sentiment
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import com.cybozu.labs.langdetect.DetectorFactory
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }

import scala.collection.JavaConversions._
import scala.util.Try

object SentimentAnalysisMain extends SentimentAnalysis with Serializable {
  def main(args: Array[String]) {
    import org.apache.log4j.Logger
    import org.apache.log4j.Level

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    analyzeTweets(args)
  }
}

trait SentimentAnalysis {

  import OAuthConfig._

  def analyzeTweets(filters: Array[String]) = {
    val conf = new SparkConf().setAppName("SentimentTweetDemo")
    val ssc = new StreamingContext(conf, Seconds(5))

    val tweets = TwitterUtils.createStream(ssc, someAuth, filters)
    val englishTweets = tweets.filter(s => detectLanguage(s.getText).exists(_.equalsIgnoreCase("en")))

    DetectorFactory.loadProfile("/home/pk/src/rnd_scala/twitterspark/src/main/resources/profiles")

    englishTweets.map(s => predictSentiment(s.getText)).print()

    ssc.start()
    ssc.awaitTermination()
  }

  val nlpProps = {
    val p = new Properties()
    p.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    p
  }

  def detectLanguage: String => Option[String] = { text: String =>
    val detector = DetectorFactory.create()
    detector.append(text)

    Try(detector.detect().toLowerCase).toOption
  }

  def nlpPipeline = {
    // disable CoreNLP log spam during creating a pipeline
    RedwoodConfiguration.empty().capture(System.err).apply()
    try { new StanfordCoreNLP(nlpProps) }
    finally { RedwoodConfiguration.current().clear().apply() }
  }

  def predictSentiment = { text: String =>
    val pipeline = nlpPipeline

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