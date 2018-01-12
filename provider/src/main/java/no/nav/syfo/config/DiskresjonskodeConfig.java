package no.nav.syfo.config;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.syfo.mocks.DiskresjonskodeMock;
import no.nav.tjeneste.pip.diskresjonskode.DiskresjonskodePortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;

@Configuration
public class DiskresjonskodeConfig {
    private static final String MOCK_KEY = "diskresjonskodev1.withmock";
    private static final String ENDEPUNKT_URL = getProperty("diskresjonskode.endpoint.url");
    private static final String ENDEPUNKT_NAVN = "DISKRESJONSKODE_V1";

    @Bean
    public DiskresjonskodePortType diskresjonskodeV1() {
        DiskresjonskodePortType prod = factory().configureStsForOnBehalfOfWithJWT().build();
        DiskresjonskodePortType mock = new DiskresjonskodeMock();

        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, DiskresjonskodePortType.class);
    }

    private CXFClient<DiskresjonskodePortType> factory() {
        return new CXFClient<>(DiskresjonskodePortType.class).address(ENDEPUNKT_URL);
    }
}
