package no.nav.syfo.config

import no.nav.syfo.LocalApplicationConfig
import no.nav.syfo.services.LdapService
import org.mockito.Mockito
import org.springframework.context.annotation.*

@Configuration
@Profile("test")
@Import(LocalApplicationConfig::class)
class TestApplicationConfig {
    @Bean
    @Primary
    fun ldapServiceMock(): LdapService {
        return Mockito.mock(LdapService::class.java)
    }
}
