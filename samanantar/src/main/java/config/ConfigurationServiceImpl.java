package config;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jdk.nashorn.internal.runtime.regexp.joni.Config;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class ConfigurationServiceImpl implements ConfigurationService
{
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static Configuration configuration;

    public ConfigurationServiceImpl()
    {
        final Path configPath = Paths.get( "appsettings.json");
        try (final BufferedReader reader = Files.newBufferedReader(configPath))
        {
            configuration = new Gson().fromJson(reader, Configuration.class);
            mongoClient = MongoClients.create(configuration.getMongoConnectionString());
            mongoDatabase = mongoClient.getDatabase("samanantar");
        } catch (IOException e) {
            System.out.println("Could not read the config file");
            e.printStackTrace();
        }
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    public void close()
    {
        mongoClient.close();
    }
}
