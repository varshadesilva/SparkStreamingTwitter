# SparkStreamingTwitter
In this project, a Spark streaming application is created that will continuously read data from Twitter about a topic. These twitter feeds are analyzed for their sentiment and visualized using Elasticsearch, Logstash and kibana.

To run this application, a Twitter app needs to be created and credentials have to be obtained. This can be done at https://apps.twitter.com/

Software Requirements:
* Kafka 2.12-2.5.0
* ElasticSearch-7.8.0
* Kibana 7.8.0-windows-x86_64
* Logstash-7.8.0

Execution steps:
1.	Navigate to the Kafka 2.12-2.5.0 directory
2.	Run the following command to start a ZooKeeper server: 
``` bin\windows\zookeeper-server-start.bat config\zookeeper.properties```
3.	Run the following command to start the Kafka server: 
```bin\windows\kafka-server-start.sh config\server.properties```
4.	Create a producer with a topic name with the following command: 
```bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic topicA``` 
5.	Run the consumer with the following command:
```bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic topicA --from-beginning``` 
6.	Navigate to the elasticsearch-7.8.0 bin directory and run the command ```elasticsearch``` to start the elasticsearch server.
7.	Navigate to the kibana 2.12-2.5.0 bin directory and run the command ```kibana``` to start the kibana server
8.	Create a logstash-simple.conf file in the logstash-7.8.0 directory with the following configurations. Any name can be given as the index.
input {
kafka {
bootstrap_servers => "localhost:9092"
topics => ["topicA"]
}
}
output {
elasticsearch {
hosts => ["localhost:9200"]
index => "myindex"
}
}
9.	Navigate to the logstash-7.8.0 directory and run the following command to start the logstash server:  ```bin\logstash -f logstash-simple.conf```
10.	 Import the project SparkStreamingTwitter to an IDE for example IntelliJ IDEA. Package the project into a fat JAR file by executing the “assembly” sbt task.
11.	In command prompt, navigate to the folder containing the fat JAR file and run the following command to run the project. The topic name created above and the consumer key, consumer secret, accessToken, and tokenSecret obtained from twitter are given as arguments to the project. 

``` spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.5 --class TwitterStreaming BigDataAssignThree-assembly-0.1.jar topicA twitterConsumerKey twitterConsumerSecret twitterAccessToken twitterAccessTokenSecret```

12.	If everything is set up correctly and the consumer is running, live tweets will be seen along with their sentiments. To visualize the data, navigate to http://localhost5601. The elasticsearch index created above will be displayed. Copy and paste the displayed index in the index patten field to create an index pattern. Click Next. In step 2 Configure settings, select the default @timestamp and click create index pattern. Navigate to the display tab to see results and visualize tab to generate visualizations.



