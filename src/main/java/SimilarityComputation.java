import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.ReadConfig;
import com.mongodb.spark.config.WriteConfig;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anagnoad on 04-Jan-17.
 */
public class SimilarityComputation {

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Twitter-Community-Detection")
                .master("local[*]")
                .getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        HashMap<String, String> readHashMap = new HashMap<>();
        readHashMap.put("uri", "mongodb://localhost:27017/twitterDB.tweets?readPreference=primaryPreferred");
        ReadConfig readConfig = ReadConfig.create(jsc).withOptions(readHashMap);

        Dataset<Row> tweets = MongoSpark.load(jsc, readConfig).toDF();

        tweets.registerTempTable("tweets");
    }

}
