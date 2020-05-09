import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.ApplicationModule;
import config.Configuration;
import config.ConfigurationService;
import config.ConfigurationServiceImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ConcurrentUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application
{
    private ConfigurationService configService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static List<String> colors = Arrays.asList("white", "black", "green", "brown", "orange", "red");

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    public Application()
    {
        LOGGER.info("Application has been initialzied");
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




    private void insertData()
    {
        try
        {
            MongoClient mongoClient = configService.getMongoClient();
            MongoDatabase mongoDatabase = configService.getDatabase();
            MongoCollection<Document> collection = mongoDatabase.getCollection("pets");
            //Inserting one document
            String petName = getPetName();
            Document doc = new Document("name", petName )
                    .append("weigh(LB)", (int)(Math.random() * (100 - 5 + 1) + 5))
                    .append("age", (int)(Math.random() * (50 - 1 + 1) + 1))
                    .append("colors", colors.get((int)(Math.random() * (4 - 0 + 1) + 0)))
                    .append("owner", new Document("name", petName+"_owner").append("address", petName+"_home"));
            LOGGER.info("Inserting Data For Pet: Name: {}", petName);
            collection.insertOne(doc);
        }
        catch(Exception ex)
        {
           LOGGER.error("Exception Thrown: {} ", ex.toString());
        }
    }


    public void run()
    {
        Runnable task = () -> {

            LOGGER.info("Scheduling insert to database");
            insertData();
        };
        //Inserting to database every 10 seconds.
        scheduledExecutorService.scheduleWithFixedDelay(task, 0, 5, TimeUnit.SECONDS);

    }



    private String getPetName() {
        int length = (int)(Math.random() * (15 - 3 + 1) + 3);
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }


    public void close()
    {
        configService.close();
        ConcurrentUtils.stop(scheduledExecutorService);
    }


}
