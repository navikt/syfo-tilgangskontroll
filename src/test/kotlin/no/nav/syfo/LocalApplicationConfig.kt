package no.nav.syfo

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate

@Configuration
@Profile("local", "test")
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
