/**
 * Program entry point
 * */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.*;
import org.bson.Document;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

public class TwitterDataCollection {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(TwitterDataCollection.class.getClass());
        ObjectMapper mapper = new ObjectMapper();

        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("TwitterStreamingInput");
        JavaStreamingContext  jssc = new JavaStreamingContext(conf, new Duration(5000));
        Configuration twitterConf = ConfigurationContext.getInstance();
        Authorization twitterAuth = AuthorizationFactory.getInstance(twitterConf);

//        String[] trends = getTrends(twitterConf);
        String[] trends = { "#trump", "#obama", "#brexit", "#italyreferendum", "#Austrianelection" };
        if (trends == null) {
            // Exceeded quota
            System.exit(-1);
        }

        JavaReceiverInputDStream<Status> twitterStream = TwitterUtils.createStream(
                jssc,
                twitterAuth,
                trends);

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
        try {
            jssc.start();
            jssc.awaitTermination();
        }
        catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }

    public static String[] getTrends(Configuration twitterConf) {
        TwitterFactory tf = new TwitterFactory(twitterConf);
        Twitter twitter = tf.getInstance();
        try {
            // 44418 is London
            // 1 is supposed to be global but gives crappy hashtags, including RTL, japanese, etc.
            // Instead of this, we can use
            // twitter.getAvailableTrends() which gives us a list of Locations, from which we can pick woeid's.
            Trend[] trends = twitter.getPlaceTrends(23424833).getTrends();
            String[] sTrends = new String[trends.length];
            for (int i=0; i< trends.length; ++i) {
                sTrends[i] = trends[i].getName();
            }
            return sTrends;
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
