import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher;
import localhost.ApplicationConfigTest;
import no.nav.brukerdialog.security.context.CustomizableSubjectHandler;
import no.nav.brukerdialog.security.domain.IdentType;

import static io.prometheus.client.CollectorRegistry.defaultRegistry;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.apiapp.ApiApp.startApp;
import static no.nav.brukerdialog.security.context.CustomizableSubjectHandler.*;
import static no.nav.brukerdialog.security.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.brukerdialog.tools.ISSOProvider.getIDToken;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;
import static no.nav.testconfig.ApiAppTest.setupTestContext;

public class MainTest {
    public static void main(String[] args){
        setupTestContext();
        setFrom("environment.properties");
        setProperty(SUBJECTHANDLER_KEY, CustomizableSubjectHandler.class.getName());
        setUid(getProperty("veileder.username"));
        setInternSsoToken(getIDToken(getProperty("veileder.username"), getProperty("veileder.password")));
        setIdentType(IdentType.InternBruker);

        HystrixPrometheusMetricsPublisher.builder().withRegistry(defaultRegistry).buildAndRegister();


        String[] _args = {"8586"};
        startApp(ApplicationConfigTest.class, _args);
    }
}
