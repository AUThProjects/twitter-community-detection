import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

/**
 * Created by anagnoad on 04-Jan-17.
 */
public class MongoToPostgres {

    public static void main(String[] args) {
        MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase database = client.getDatabase("twitterDB");
        MongoCollection<Document> collection = database.getCollection("tweets");

        Block<Document> convertBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                Document user = (Document)document.get("user");

                Long userId = ((Number) user.get("id")).longValue();

                List<Document> hashtags = (List<Document>)document.get("hashtagEntities");
                List<String> hashtagsText = new ArrayList<>();
                for(Document h : hashtags) {
                    hashtagsText.add(h.getString("text").toLowerCase());
                }

                List<Document> urls = (List<Document>)document.get("urlentities");
                List<String> urlsText = new ArrayList<>();
                for(Document u : urls) {
                    urlsText.add(u.getString("url").toLowerCase());
                }

                List<Document> mentions = (List<Document>)document.get("userMentionEntities");
                List<Long> mentionsIds = new ArrayList<>();
                for(Document m: mentions) {
                    mentionsIds.add(((Number)m.get("id")).longValue());
                }

                Document retweet = (Document)document.get("retweetedStatus");
                Long retweetId = null;
                if (retweet!=null ) {
                    retweetId = ((Number) retweet.get("id")).longValue();
                }
//                System.out.println(userId + " " +
//                        String.join(",", hashtagsText) + " " +
//                        Arrays.toString(mentionsIds.toArray()) + " " +
//                        String.join(",", urlsText) +  " " +
//                        (retweetId!=null? retweetId: "null")
//                );
                try {
                    Class.forName("org.postgresql.Driver");
                }
                catch (ClassNotFoundException e) {
                    System.out.println(e);
                    return;
                }
                Connection connection = null;
                try {
                    connection = DriverManager.getConnection(
                            "jdbc:postgresql://localhost:5432/twitterdb", "twitteruser", "twitterpass");

                    if (!hashtagsText.isEmpty()) {
                        Statement stmt = connection.createStatement();
                        StringBuilder sb = new StringBuilder("INSERT INTO user_hashtag(uid, hashtag) VALUES ");
                        for (String hashtag : hashtagsText) {
                            sb.append(String.format("('%d', '%s'),", userId, hashtag));
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        System.out.println(sb.toString());
                        stmt.executeUpdate(sb.toString());
                    }

                    if (!mentionsIds.isEmpty()) {
                        Statement stmt = connection.createStatement();
                        StringBuilder sb = new StringBuilder("INSERT INTO user_mention(uid, mention_id) VALUES ");
                        for (Long mention_uid : mentionsIds) {
                            sb.append(String.format("('%d', '%d'),", userId, mention_uid));
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        System.out.println(sb.toString());
                        stmt.executeUpdate(sb.toString());
                    }
                    if (!urlsText.isEmpty()) {
                        Statement stmt = connection.createStatement();
                        StringBuilder sb = new StringBuilder("INSERT INTO user_url(uid, url) VALUES ");
                        for (String url : urlsText) {
                            sb.append(String.format("('%d', '%s'),", userId, url));
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        System.out.println(sb.toString());
                        stmt.executeUpdate(sb.toString());
                    }
                    if (retweetId!=null) {
                        Statement stmt = connection.createStatement();
                        String sql ="INSERT INTO user_retweet(uid, retweet_id) VALUES ('"+ userId + "','" + retweetId + "')";
                        System.out.println(sql);
                        stmt.executeUpdate(sql);
                    }
                }
                catch(SQLException e) {
                    System.err.println(e.getMessage());
                }
                finally {
                    try {
                        connection.close();
                    }
                    catch (SQLException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        };

        collection.find()
                .projection(fields(include("user.id", "userMentionEntities", "retweetedStatus.id", "urlentities", "hashtagEntities"), excludeId()))
                .limit(1000)
                .forEach(convertBlock);
    }
}
