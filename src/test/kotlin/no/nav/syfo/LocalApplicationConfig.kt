package no.nav.syfo

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import no.nav.syfo.consumer.ldap.LdapService
import org.mockito.Mockito
import org.springframework.context.annotation.*

@Configuration
@Profile("local", "test")
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig {
    @Bean
    @Primary
    fun ldapServiceMock(): LdapService {
        return Mockito.mock(LdapService::class.java)
    }
}
