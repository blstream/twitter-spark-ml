# Spark/Twitter analysis demo

## How to run the code

### Build with sbt assembly

Tip: in case of OOM increase the heap size with ``-J-Xmx...``

```scala
sbt assembly
```

## Language prediction/detection

### Get some tweets from current stream

```shell
<SPARK_HOME>/bin/spark-submit --class "com.blstream.twitterspark.DataImporterMain" --master local[4] target/scala-2.11/twitterspark-assembly-0.0.1.jar
```

### Train

Go to Zeppelin notebook in `zeppelin-notebook.json`. To import you'll need Apache Zeppelin version 0.6.0 (at least).

### Predict

```shell
<SPARK_HOME>/bin/spark-submit --class "com.blstream.twitterspark.PredictLangMain" --master local[4] target/scala-2.11/twitterspark-assembly-0.0.1.jar
```

## Live sentiment analysis

To see a live stream of English tweets, with classified sentiment, run the following command:

```shell
$SPARK_HOME/bin/spark-submit --class "com.blstream.twitterspark.SentimentAnalysisMain" --master "local[4]" target/scala-2.11/twitterspark-assembly-0.0.1.jar
```

with `SPARK_HOME` set to point at your downloaded Apache Spark copy.