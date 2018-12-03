package no.nav.syfo;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import no.nav.syfo.mocks.*;
import no.nav.tjeneste.pip.diskresjonskode.DiskresjonskodePortType;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.OrganisasjonRessursEnhetV1;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;

@Configuration
@Profile("local")
@Import(TokenGeneratorConfiguration.class)
public class LocalApplicationConfig {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestInterceptor... interceptors) {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(asList(interceptors));
        return template;
    }

    @Bean
    public OrganisasjonEnhetV2 organisasjonEnhetV2Mock () {
        return new OrganisasjonEnhetMock();
    }

    @Bean
    public EgenAnsattV1 egenAnsattV1Mock() {
        return new EgenansattMock();
    }

    @Bean
    public DiskresjonskodePortType diskresjonskodeV1Mock() {
        return new DiskresjonskodeMock();
    }

    @Bean
    public OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1Mock() {
        return new OrganisasjonRessursEnhetMock();
    }

    @Bean
    public PersonV3 personV3Mock() {
        return new PersonMock();
    }

}
