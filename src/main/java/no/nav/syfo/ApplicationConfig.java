package no.nav.syfo;

import no.nav.syfo.ws.LogErrorHandler;
import no.nav.syfo.ws.STSClientConfig;
import no.nav.syfo.ws.WsClient;
import no.nav.syfo.ws.WsOIDCClient;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.OrganisasjonRessursEnhetV1;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static java.util.Collections.singletonList;

@Configuration
@Profile("remote")
@SuppressWarnings("unchecked")
public class ApplicationConfig {

    /* Web service clients */

    @Bean
    @Primary
    public OrganisasjonEnhetV2 organisasjonEnhetV2(@Value("${organisasjonEnhet.v2.url}") String serviceUrl) {
        OrganisasjonEnhetV2 port = new WsClient<OrganisasjonEnhetV2>().createPort(serviceUrl, OrganisasjonEnhetV2.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @Bean
    @Primary
    public EgenAnsattV1 egenAnsattV1(@Value("${egenansatt.v1.url}") String serviceUrl) {
        EgenAnsattV1 port = new WsClient<EgenAnsattV1>().createPort(serviceUrl, EgenAnsattV1.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @Bean
    @Primary
    public OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1(@Value("${organisasjonressursenhet.v1.url}") String serviceUrl) {
        OrganisasjonRessursEnhetV1 port = new WsClient<OrganisasjonRessursEnhetV1>().createPort(serviceUrl, OrganisasjonRessursEnhetV1.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @Bean
    @Primary
    public PersonV3 personV3(@Value("${person.v3.url}") String serviceUrl) {
        PersonV3 port = new WsClient<PersonV3>().createPort(serviceUrl, PersonV3.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }
}
