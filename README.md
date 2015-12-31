# How to run the code

# Status
## todo

* read tweets from Riak to Zeppelin notebook
* save trained model to Riak
* use model from Riak to predict language

# Installation and configuration

## Riak
### Install on Ubuntu 15.10
Download Debian Wheezy package from [Basho downloads page](https://docs.basho.com/riak/2.1.3/downloads/) and install

### Create and activate bucket type
```shell
sudo riak-admin bucket-type create tweet-sets '{"props":{"datatype":"set"}}'
sudo riak-admin bucket-type activate tweet-sets
```


## spark-riak-connector
Copy from `<PROJECT_HOME>/lib` to local maven repo files:

* `spark-riak-connector_2.10-1.1.0.jar` to `~/.m2/repository/com/basho/riak/spark-riak-connector_2.10/1.1.0/`
* `spark-riak-connector-java_2.10-1.1.0.jar` to `~/.m2/repository/com/basho/riak/spark-riak-connector-java_2.10/1.1.0/`

## Build with sbt assembly

```scala
sbt assembly
```

# How to use demo

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
