/**
 * Program entry point
 * */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.*;
import org.bson.Document;
import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

import javax.xml.crypto.Data;
import java.io.Console;


public class TwitterCommunityDetection {
    public static void main(String[] args) {
        Database db = new Database();
        Logger logger = Logger.getLogger(TwitterCommunityDetection.class.getClass());
        ObjectMapper mapper = new ObjectMapper();

        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("TwitterStreamingInput");
        JavaStreamingContext  jssc = new JavaStreamingContext(conf, new Duration(5000));
        Configuration twitterConf = ConfigurationContext.getInstance();
        Authorization twitterAuth = AuthorizationFactory.getInstance(twitterConf);

        String[] filters = { "obama", "trump" };

        JavaReceiverInputDStream<Status> twitterStream = TwitterUtils.createStream(jssc, twitterAuth, filters);
        twitterStream.foreachRDD( s -> {
            s.foreachPartition( p -> {
                MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
                MongoDatabase database = client.getDatabase("twitterDB");
                MongoCollection<Document> collection = database.getCollection("tweets");
                while(p.hasNext()) {
                    Status i = p.next();
                    String stringifiedTweet = mapper.writeValueAsString(i);
                    Document doc = Document.parse(stringifiedTweet);
                    collection.insertOne(doc);
                }
                client.close();
            });
        });
        jssc.start();
        jssc.awaitTermination();
    }
}
