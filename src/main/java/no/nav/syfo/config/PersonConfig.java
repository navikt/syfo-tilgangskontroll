package no.nav.syfo.config;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.syfo.mocks.PersonMock;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class PersonConfig {

    private static final String MOCK_KEY = "person.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_PERSON_V3_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "PERSON_V3";
    private static final boolean KRITISK = true;

    @Bean
    public PersonV3 personV3() {
        PersonV3 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        PersonV3 mock = new PersonMock();

        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, PersonV3.class);
    }

    @Bean
    public Pingable personPing() {
        Pingable.Ping.PingMetadata pingMetadata = new Pingable.Ping.PingMetadata(ENDEPUNKT_URL, ENDEPUNKT_NAVN, KRITISK);
        final PersonV3 personV3 = factory()
                .configureStsForSystemUserInFSS()
                .build();
        return () -> {
            try {
                personV3.ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<PersonV3> factory() {
        return new CXFClient<>(PersonV3.class).address(ENDEPUNKT_URL);
    }
}

