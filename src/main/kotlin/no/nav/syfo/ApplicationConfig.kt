package no.nav.syfo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.retry.annotation.EnableRetry
import org.springframework.web.client.RestTemplate

@Configuration
@EnableRetry
@Profile("remote")
class ApplicationConfig {
    @Bean
    @Primary
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
