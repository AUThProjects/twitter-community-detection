import scala.Tuple2;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anagnoad on 04-Jan-17.
 */
public class SimilarityComputation {

    public static void main (String[] args) {

        HashMap<String, Double> magnitudes = new HashMap<>();
        HashMap<Tuple2<String, String>, Double> similarities = new HashMap<>();

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

            Statement stmt = connection.createStatement();
            String hashtagCountSql = "SELECT count(distinct hashtag) FROM user_hashtag";
            ResultSet rs = stmt.executeQuery(hashtagCountSql);
            rs.next();
            int totalHashtags = rs.getInt("count");
            System.out.println(totalHashtags);
            stmt = connection.createStatement();
            String sql = "SELECT uid, count(hashtag) FROM user_hashtag GROUP BY uid";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String uid = rs.getString("uid");
                int count = rs.getInt("count");
//                System.out.println(uid + " " + count);
                double magnitude = count * 1.0 / totalHashtags;
                magnitudes.put(uid, magnitude);
            }
//            System.out.println(magnitudes.size());

            String similarSql = "select uid1, uid2, hashtag, user1_hashtag_count, user2_hashtag_count\n" +
                    "from (\n" +
                    "  select uh1.uid as uid1, uh2.uid as uid2, uh1.hashtag as hashtag, uh1.count as user1_hashtag_count, uh2.count as user2_hashtag_count\n" +
                    "  from (\n" +
                    "    select uh1.uid, uh1.hashtag, count(uh1.hashtag)\n" +
                    "    from user_hashtag as uh1\n" +
                    "    group by uh1.uid, uh1.hashtag  \n" +
                    "  ) as uh1\n" +
                    "  inner join (\n" +
                    "  select uh2.uid, uh2.hashtag, count(uh2.hashtag)\n" +
                    "  from user_hashtag as uh2 \n" +
                    "  group by uh2.uid, uh2.hashtag\n" +
                    "  ) as uh2\n" +
                    "  on uh1.hashtag=uh2.hashtag \n" +
                    "  where uh1.uid < uh2.uid\n" +
                    "  order by uh1.uid asc, uh2.uid asc\n" +
                    ") as q\n" +
                    "\n";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(similarSql);

            String uid1 = "";
            String uid2 = "";
            int acc = 0;
            while (rs.next()) {
               String nextUid1 = rs.getString("uid1");
               String nextUid2 = rs.getString("uid2");
//                System.out.println(nextUid1 + " " + nextUid2);
//                System.out.println(magnitudes.get(nextUid1));
//                for (Map.Entry<String, Double> entry: magnitudes.entrySet()) {
//                    System.out.println(entry.getKey() + "," + entry.getValue());
//                }

                if (nextUid2 != uid2 || nextUid1 != uid1) {
                    similarities.put(new Tuple2<>(uid1, uid2), (double) acc/(magnitudes.get(nextUid1) * magnitudes.get(nextUid2)));
                    acc = 0;
                }

               int count1= rs.getInt("user1_hashtag_count");
               int count2= rs.getInt("user2_hashtag_count");
               acc += count1*count2;

               uid1=nextUid1;
               uid2=nextUid2;
            }

            for (Map.Entry<Tuple2<String, String>, Double> entry: similarities.entrySet()) {
                stmt = connection.createStatement();
                String insertSimilarity = String.format("INSERT INTO cosine_similarity(uid_r,uid_c,similarity) VALUES(%s, %s, %f)", entry.getKey()._1(), entry.getKey()._2(), entry.getValue());
                stmt.executeUpdate(insertSimilarity);
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

}
