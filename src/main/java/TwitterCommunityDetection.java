/**
 * Program entry point
 * */

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.*;
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
        TwitterUtils.createStream(jssc, twitterAuth, filters).map(
            s -> TwitterObjectFactory.getRawJSON(s)
        ).print();
        jssc.start();
        jssc.awaitTermination();
    }
}
