package no.nav.syfo;

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;

@Configuration
@Profile({"local", "test"})
@Import(TokenGeneratorConfiguration.class)
public class LocalApplicationConfig {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestInterceptor... interceptors) {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(asList(interceptors));
        return template;
    }
}
