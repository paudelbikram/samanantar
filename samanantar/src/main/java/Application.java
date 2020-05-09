import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.ApplicationModule;
import config.Configuration;
import config.ConfigurationService;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Application
{
    private ConfigurationService configService;



    public Application()
    {
        // This constructor is used when field or method injection is used.
    }

    //@Inject this constructor is used when constructor injection is used
    public Application(ConfigurationService configService)
    {
        this.configService = configService;
    }

    @Inject //setter method injector
    public void setConfigService(ConfigurationService configService)
    {
        this.configService = configService;
    }




    public void run()
    {
        try
        {
            MongoClient mongoClient = configService.getMongoClient();
            MongoDatabase mongoDatabase = configService.getDatabase();

            MongoCollection<Document> collection = mongoDatabase.getCollection("pets");

            //Inserting one document
            Document doc = new Document("name", "kitty")
                    .append("weigh(LB)", 2)
                    .append("age", 2)
                    .append("colors", Arrays.asList("black", "white"))
                    .append("owner", new Document("name", "kitty_owner").append("address", "kitty_home"));
            collection.insertOne(doc);
            //Inserting many documents
            List<Document> documents = new ArrayList<Document>();
            for (int i = 0; i < 100; i++) {
                documents.add(new Document("i", i));
            }

            collection.insertMany(documents);
        }
        catch(Exception ex)
        {
            System.out.println("Exception Thrown "+ ex.toString());
        }
        finally {
            configService.close();
        }

    }


}
