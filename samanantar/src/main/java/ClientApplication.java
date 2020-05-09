import com.google.inject.Guice;
import com.google.inject.Injector;
import config.ApplicationModule;
import config.ConfigurationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientApplication
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientApplication.class);
    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new ApplicationModule());
        LOGGER.info("Initializing Application");
        Application application = injector.getInstance(Application.class);
        try
        {
            application.run();
        }
        catch (Exception ex)
        {
            LOGGER.error(ex.toString());
            application.close();
        }
    }
}
