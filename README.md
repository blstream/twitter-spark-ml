# How to run the code

## Build with sbt assembly

```scala
sbt assembly
```

## Get some tweets from current stream

```shell
<SPARK_HOME>/bin/spark-submit --class "com.blstream.twitterspark.DataImporterMain" --master local[4] target/scala-2.10/twitterspark-assembly-0.0.1.jar
```

## Train

Go to Zeppelin notebook

## Predict

```shell
<SPARK_HOME>/bin/spark-submit --class "com.blstream.twitterspark.PredictLangMain" --master local[4] target/scala-2.10/twitterspark-assembly-0.0.1.jar
```
