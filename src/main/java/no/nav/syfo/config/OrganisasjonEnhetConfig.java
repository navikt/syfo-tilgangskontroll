package no.nav.syfo.config;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.syfo.mocks.OrganisasjonEnhetMock;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import org.springframework.context.annotation.Bean;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

public class OrganisasjonEnhetConfig {

    private static final String MOCK_KEY = "organisasjonenhet.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_ORGANISASJONENHET_V2_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "ORGANISASJONENHET_V2";
    private static final boolean KRITISK = true;

    @Bean
    public OrganisasjonEnhetV2 organisasjonEnhetV2() {
        OrganisasjonEnhetV2 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        OrganisasjonEnhetV2 mock = new OrganisasjonEnhetMock();

        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, OrganisasjonEnhetV2.class);
    }

    @Bean
    public Pingable organisasjonEnhetV2Ping() {
        Pingable.Ping.PingMetadata pingMetadata = new Pingable.Ping.PingMetadata(ENDEPUNKT_URL, ENDEPUNKT_NAVN, KRITISK);
        final OrganisasjonEnhetV2 organisasjonEnhetV2 = factory()
                .configureStsForSystemUserInFSS()
                .build();
        return () -> {
            try {
                organisasjonEnhetV2.ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<OrganisasjonEnhetV2> factory() {
        return new CXFClient<>(OrganisasjonEnhetV2.class).address(ENDEPUNKT_URL);
    }
}
