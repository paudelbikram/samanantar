import com.google.inject.Guice;
import com.google.inject.Injector;
import config.ApplicationModule;

public class ClientApplication
{
    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new ApplicationModule());

        Application application = injector.getInstance(Application.class);

        application.run();
    }
}
