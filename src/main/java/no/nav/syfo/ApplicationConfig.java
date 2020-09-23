package no.nav.syfo;

import no.nav.syfo.ws.*;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singletonList;

@Configuration
@EnableRetry
@Profile("remote")
@SuppressWarnings("unchecked")
public class ApplicationConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /* Web service clients */

    @Bean
    @Primary
    public PersonV3 personV3(@Value("${person.v3.url}") String serviceUrl) {
        PersonV3 port = new WsClient<PersonV3>().createPort(serviceUrl, PersonV3.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }
}
