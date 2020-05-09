package config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import config.ConfigurationService;
import config.ConfigurationServiceImpl;

public class ApplicationModule extends AbstractModule
{

    @Override
    protected void configure() {
        bind(ConfigurationService.class).to(ConfigurationServiceImpl.class).asEagerSingleton();
        // could define additional bindings here
    }
}
