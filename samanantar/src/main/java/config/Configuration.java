package config;

public class Configuration
{
    private String mongoConnectionString;

    public String getMongoConnectionString() {
        return mongoConnectionString;
    }

    public void setMongoConnectionString(String mongoConnectionString) {
        this.mongoConnectionString = mongoConnectionString;
    }
}
