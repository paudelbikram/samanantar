package config;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public interface ConfigurationService {

    MongoClient getMongoClient();

    MongoDatabase getDatabase();

    void close();
}
