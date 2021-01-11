package no.nav.syfo;

import org.springframework.context.annotation.*;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

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
}
