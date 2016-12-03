import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.Serializable;

/**
 * Created by anagnoad on 12/3/2016.
 */
public class Database {

    private String dbURI = "mongodb://localhost:27017";
    private String dbName = "twitterDB";
    private String collectionName = "tweets";

    private MongoClient client;
    private MongoClientURI clientURI;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    Database() {
        this.clientURI = new MongoClientURI(this.dbURI);
        this.client = new MongoClient(this.clientURI);
        this.database = this.client.getDatabase(this.dbName);
        this.collection = database.getCollection(this.collectionName);
    }

    public void print() {
        this.collection.find().forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document.toJson());
            }
        });
    }

    public void insert(String jsonObject) {
        try {
            Document doc = Document.parse(jsonObject);
            this.collection.insertOne(doc);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public void insert(String[] jsonObjects) {
        for (String rawObject: jsonObjects) {
            this.insert(rawObject);
        }
    }

    public void closeConnections() {
        this.client.close();
    }
}
