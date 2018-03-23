package localhost;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.syfo.config.ApplicationConfig;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.util.EnumSet;

@Configuration
public class ApplicationConfigTest extends ApplicationConfig {

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .sts();
    }

    @Override
    public void startup(ServletContext servletContext) {
        servletContext.addFilter("corsFilter", CORSFilter.class)
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");;
    }

    @Override
    public ApiApplication.Sone getSone() {
        return null;
    }
}
