/**
 * Program entry point
 * */

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
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;


public class TwitterCommunityDetection {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(TwitterCommunityDetection.class.getClass());
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("TwitterStreamingInput");
        JavaStreamingContext  jssc = new JavaStreamingContext(conf, new Duration(5000));
        Configuration twitterConf = ConfigurationContext.getInstance();
        Authorization twitterAuth = AuthorizationFactory.getInstance(twitterConf);

        String[] filters = { "obama", "trump" };

        JavaReceiverInputDStream<Status> twitterStream = TwitterUtils.createStream(jssc, twitterAuth, filters);
        twitterStream.foreachRDD( s -> {
            s.foreachPartition( p -> {
                MongoClient client = new MongoClient(new MongoClientURI("mongodb://host:27017"));
                MongoDatabase database = client.getDatabase("db_name");
                MongoCollection<Document> collection = database.getCollection("coll_name");
                while(p.hasNext()) {
                    Status i = p.next();
                    Document doc = Document.parse(TwitterObjectFactory.getRawJSON(i));
                    collection.insertOne(doc);
                }
                client.close();
            });
        });
        jssc.start();
        jssc.awaitTermination();
    }
}
