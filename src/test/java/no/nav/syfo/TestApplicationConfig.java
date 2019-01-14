package no.nav.syfo;

import no.nav.syfo.services.LdapService;
import org.springframework.context.annotation.*;

import static org.mockito.Mockito.mock;

@Configuration
@Profile({"test"})
@Import(LocalApplicationConfig.class)
public class TestApplicationConfig {

    @Bean
    @Primary
    public LdapService ldapServiceMock() {
        return mock(LdapService.class);
    }

}
