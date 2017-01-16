import scala.Tuple2;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.sqrt;

/**
 * Created by anagnoad on 04-Jan-17.
 */
public class SimilarityComputation {

    public static void main (String[] args) {
        computeCosineSimilarity("hashtag", "hashtag");
        computeCosineSimilarity("url", "url");
        computeCosineSimilarity("retweet_id", "retweet");
        computeCosineSimilarity("mention_id", "mention");
        computeAvgSimilarityMatrix("cosine");
        computeAvgSimilarityMatrix("jaccard");
    }

    public static void computeJaccardSimilarity(String column, String field) {
        String table = "user_" + field;

        HashMap<String, Double> cardinalities = new HashMap<>();
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
            stmt = connection.createStatement();
            String sql = String.format("select %1$s, uid, count(uid) as countrow from %2$s group by %1$s, uid order by uid", column, table);
            ResultSet rs = stmt.executeQuery(sql);
            double acc = 0;
            String previousUid = "";
            while (rs.next()) {
                String uid = rs.getString("uid");
                int count = rs.getInt("countrow");


                if (previousUid.equals(uid) || previousUid == "") {
                    acc += count;
                }
                else {
                    cardinalities.put(previousUid, sqrt(acc));
                    acc = count;
                }
                previousUid = uid;
            }
            cardinalities.put(previousUid, sqrt(acc));

            String similarSql = String.format("select uid1, uid2, %2$s, user1_%2$s_count, user2_%2$s_count\n" +
                    "from (\n" +
                    "  select uh1.uid as uid1, uh2.uid as uid2, uh1.%2$s as %2$s, uh1.count as user1_%2$s_count, uh2.count as user2_%2$s_count\n" +
                    "  from (\n" +
                    "    select uh1.uid, uh1.%2$s, count(uh1.%2$s)\n" +
                    "    from %1$s as uh1\n" +
                    "    group by uh1.uid, uh1.%2$s  \n" +
                    "  ) as uh1\n" +
                    "  inner join (\n" +
                    "  select uh2.uid, uh2.%2$s, count(uh2.%2$s)\n" +
                    "  from %1$s as uh2 \n" +
                    "  group by uh2.uid, uh2.%2$s\n" +
                    "  ) as uh2\n" +
                    "  on uh1.%2$s=uh2.%2$s \n" +
                    "  where uh1.uid < uh2.uid\n" +
                    "  order by uh1.uid asc, uh2.uid asc\n" +
                    ") as q\n", table, column);
            stmt = connection.createStatement();
            rs = stmt.executeQuery(similarSql);

            String uid1 = "";
            String uid2 = "";
            acc = 0;
            while (rs.next()) {
                String nextUid1 = rs.getString("uid1");
                String nextUid2 = rs.getString("uid2");

                if ((uid1 != "" && uid2 != "") && (!nextUid2.equals(uid2) || !nextUid1.equals(uid1))) {
                    similarities.put(new Tuple2<>(uid1, uid2), (double) acc/(cardinalities.get(uid1) + cardinalities.get(uid2) - acc));
                    acc = 0;
                }

                int count1= rs.getInt(String.format("user1_%1$s_count", column));
                int count2= rs.getInt(String.format("user2_%1$s_count", column));
                acc += Math.min(count1, count2);

                uid1=nextUid1;
                uid2=nextUid2;
            }

            for (Map.Entry<Tuple2<String, String>, Double> entry: similarities.entrySet()) {
                stmt = connection.createStatement();
                String insertSimilarity = String.format("INSERT INTO jaccard_similarity_%4$s(uid_r,uid_c,similarity) VALUES(%s, %s, %f)", entry.getKey()._1(), entry.getKey()._2(), entry.getValue(), field);
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

    public static void computeCosineSimilarity(String column, String field) {
        String table = "user_"+field;

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
            stmt = connection.createStatement();
            String sql = String.format("select %1$s, uid, count(uid) as countrow from %2$s group by %1$s, uid order by uid", column, table);
            ResultSet rs = stmt.executeQuery(sql);
            double acc = 0;
            String previousUid = "";
            while (rs.next()) {
                String uid = rs.getString("uid");
                int count = rs.getInt("countrow");


                if (previousUid.equals(uid) || previousUid == "") {
                    acc += Math.pow(count,2);
                }
                else {
                    magnitudes.put(previousUid, sqrt(acc));
                    acc = Math.pow(count,2);
                }
                previousUid = uid;
            }
            magnitudes.put(previousUid, sqrt(acc));

            String similarSql = String.format("select uid1, uid2, %2$s, user1_%2$s_count, user2_%2$s_count\n" +
                    "from (\n" +
                    "  select uh1.uid as uid1, uh2.uid as uid2, uh1.%2$s as %2$s, uh1.count as user1_%2$s_count, uh2.count as user2_%2$s_count\n" +
                    "  from (\n" +
                    "    select uh1.uid, uh1.%2$s, count(uh1.%2$s)\n" +
                    "    from %1$s as uh1\n" +
                    "    group by uh1.uid, uh1.%2$s  \n" +
                    "  ) as uh1\n" +
                    "  inner join (\n" +
                    "  select uh2.uid, uh2.%2$s, count(uh2.%2$s)\n" +
                    "  from %1$s as uh2 \n" +
                    "  group by uh2.uid, uh2.%2$s\n" +
                    "  ) as uh2\n" +
                    "  on uh1.%2$s=uh2.%2$s \n" +
                    "  where uh1.uid < uh2.uid\n" +
                    "  order by uh1.uid asc, uh2.uid asc\n" +
                    ") as q\n", table, column);
            stmt = connection.createStatement();
            rs = stmt.executeQuery(similarSql);

            String uid1 = "";
            String uid2 = "";
            acc = 0;
            while (rs.next()) {
                String nextUid1 = rs.getString("uid1");
                String nextUid2 = rs.getString("uid2");

                if ((uid1 != "" && uid2 != "") && (!nextUid2.equals(uid2) || !nextUid1.equals(uid1))) {
                    similarities.put(new Tuple2<>(uid1, uid2), (double) acc/(magnitudes.get(uid1) * magnitudes.get(uid2)));
                    acc = 0;
                }

                int count1= rs.getInt(String.format("user1_%1$s_count", column));
                int count2= rs.getInt(String.format("user2_%1$s_count", column));
                acc += count1*count2;

                uid1=nextUid1;
                uid2=nextUid2;
            }

            for (Map.Entry<Tuple2<String, String>, Double> entry: similarities.entrySet()) {
                stmt = connection.createStatement();
                String insertSimilarity = String.format("INSERT INTO cosine_similarity_%4$s(uid_r,uid_c,similarity) VALUES(%s, %s, %f)", entry.getKey()._1(), entry.getKey()._2(), entry.getValue(), field);
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

    public static void computeAvgSimilarityMatrix(String metric) {

        String[] simTables = {
                String.format("%s_similarity_hashtag", metric),
                String.format("%s_similarity_url", metric),
                String.format("%s_similarity_mention", metric),
                String.format("%s_similarity_retweet", metric)};
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

            for (String table : simTables) {
                Statement stmt = connection.createStatement();
                stmt = connection.createStatement();
                String sql = String.format("select * from %s", table);
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String uid1 = rs.getString("uid_r");
                    String uid2 = rs.getString("uid_c");
                    double similarity = 0.25 * rs.getDouble("similarity");
                    Statement inner_stmt = connection.createStatement();
                    String retrieve_sql = String.format("select * from %s_similarity where uid_r = '%s' and uid_c = '%s'", metric, uid1, uid2);
                    ResultSet retrieved = inner_stmt.executeQuery(retrieve_sql);
                    if (retrieved.next()) {
                        double oldSimilarity = retrieved.getDouble("similarity");
                        double newSimilarity = oldSimilarity + similarity;
                        String updateSql = String.format("update %s_similarity set similarity=%f where uid_r = '%s' and uid_c = '%s'", metric, newSimilarity, uid1, uid2);
                        inner_stmt.executeUpdate(updateSql);
                    }
                    else {
                        String updateSql = String.format("insert into %s_similarity(uid_r, uid_c, similarity) values(%s, %s, %f)", metric, uid1, uid2, similarity);
                        inner_stmt.executeUpdate(updateSql);
                    }
                }
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
