package no.nav.syfo.testhelper

import no.nav.syfo.domain.AdRoller
import no.nav.syfo.ldap.LdapService
import org.mockito.Mockito

object LdapUtil {
    @JvmStatic
    fun mockRoller(ldapService: LdapService, veileder: String, innvilget: Boolean, vararg roller: AdRoller) {
        Mockito.reset(ldapService)
        for (rolle in roller) {
            Mockito.`when`(ldapService.harTilgang(veileder, rolle.rolle)).thenReturn(innvilget)
        }
    }
}
