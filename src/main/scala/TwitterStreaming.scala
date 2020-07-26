import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object TwitterStreaming {
  def main(args: Array[String]): Unit = {
    //setting the spark configuration
    val sparkConf = new SparkConf().setAppName("Twitter-Streaming").setMaster("local[*]")
      .set("spark.driver.host", "localhost")

    //program exits if 5 input parameters are not received
    if (args.length < 5) {
      println("Correct usage: Program_Name inputTopic twitter_consumerKey twitter_consumerSecret twitter_accessToken twitter_accessTokenSecret")
      System.exit(1)
    }

    //setting the topic
    val inTopic = args(0)

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.WARN)

    val consumerKey = args(1)
    val consumerSecret = args(2)
    val accessToken = args(3)
    val accessTokenSecret = args(4)

    //Twitter search topics
    val filters = Seq("Biden", "Trump")

    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val tweetStream = TwitterUtils.createStream(ssc, None, filters, StorageLevel.MEMORY_ONLY_SER_2)
    val englishTweets = tweetStream.filter(_.getLang() == "en")
    val tweetText = englishTweets.map(tweet => (tweet.getText()))

    //Dstream (continuous stream of data) is represented by a continuous series of RDDs. Each RDD is partitioned across
    //different nodes. For each element in a partition, the sentiment is analyzed and a record is created. Kafka producer
    //publishes record into topic.

    tweetText.foreachRDD { rdd =>
      rdd.foreachPartition { partitionOfRecords =>
        //setting properties and creating a producer only once per RDD partition
        val props = new Properties()
        val bootstrap = "localhost:9092"
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("bootstrap.servers", bootstrap)

        val producer = new KafkaProducer[String, String](props)

        partitionOfRecords.foreach { element =>
          val message = element
          val sentiment = SentimentAnalyzer.mainSentiment(message)
          val record = new ProducerRecord[String, String](inTopic, "sentiment", sentiment + "--" +  message)
          producer.send(record)
        }
        producer.flush()
        producer.close()

      }
    }

    ssc.start()
    ssc.awaitTermination()

  }
}

