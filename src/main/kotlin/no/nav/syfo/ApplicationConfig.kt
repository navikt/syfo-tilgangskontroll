package no.nav.syfo

import org.springframework.context.annotation.*
import org.springframework.web.client.RestTemplate

@Configuration
class ApplicationConfig {
    @Bean(name = ["default"])
    @Primary
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
