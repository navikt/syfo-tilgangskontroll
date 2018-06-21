import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher;
import no.nav.syfo.config.ApplicationConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static io.prometheus.client.CollectorRegistry.defaultRegistry;
import static java.lang.System.*;
import static no.nav.apiapp.ApiApp.startApp;

public class Main {
    public static void main(String... args) throws Exception {
        getenv().forEach(System::setProperty);
        setProperty("SRVSYFO-TILGANGSKONTROLL_USERNAME", getenv("SRVSYFO_TILGANGSKONTROLL_USERNAME"));
        setProperty("SRVSYFO-TILGANGSKONTROLL_PASSWORD", getenv("SRVSYFO_TILGANGSKONTROLL_PASSWORD"));
        setProperty("OIDC_REDIRECT_URL", getProperty("VEILARBLOGIN_REDIRECT_URL_URL"));
        setupMetricsProperties();
        startApp(ApplicationConfig.class, args);
    }

    private static void setupMetricsProperties() throws UnknownHostException {
        setProperty("applicationName", "syfo-tilgangskontroll");
        setProperty("node.hostname", InetAddress.getLocalHost().getHostName());
        setProperty("environment.name", getProperty("FASIT_ENVIRONMENT_NAME"));

        HystrixPrometheusMetricsPublisher.builder().withRegistry(defaultRegistry).buildAndRegister();
    }
}