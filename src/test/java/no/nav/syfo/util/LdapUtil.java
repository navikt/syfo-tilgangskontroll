package no.nav.syfo.util;

import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.services.LdapService;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class LdapUtil {

    public static void mockRoller(LdapService ldapService, String veileder, boolean innvilget, AdRoller... roller) {
        reset(ldapService);

        for(AdRoller rolle : roller) {
            when(ldapService.harTilgang(veileder, rolle.rolle)).thenReturn(innvilget);
        }
    }

}
