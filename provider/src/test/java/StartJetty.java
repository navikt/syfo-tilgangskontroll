import no.nav.brukerdialog.security.context.CustomizableSubjectHandler;
import no.nav.sbl.dialogarena.common.jetty.Jetty;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.brukerdialog.security.context.CustomizableSubjectHandler.setInternSsoToken;
import static no.nav.brukerdialog.security.context.CustomizableSubjectHandler.setUid;
import static no.nav.brukerdialog.tools.ISSOProvider.getIDToken;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;
import static no.nav.sbl.dialogarena.common.jetty.JettyStarterUtils.*;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;

public class StartJetty {
    public static void main(String[] args) throws Exception {
        setFrom("environment.properties");

        setProperty("no.nav.brukerdialog.security.context.subjectHandlerImplementationClass", CustomizableSubjectHandler.class.getName());
        setUid(getProperty("veileder.username"));
        setInternSsoToken(getIDToken());

        Jetty jetty = usingWar()
                .at("syfo-tilgangskontroll")
                .port(8585)
                .disableAnnotationScanning()
                .overrideWebXml()
                .buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }
}
