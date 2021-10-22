package no.nav.syfo.testhelper

import no.nav.syfo.consumer.ldap.LdapService
import no.nav.syfo.domain.*
import org.mockito.Mockito

object LdapUtil {
    @JvmStatic
    fun mockRoller(
        ldapService: LdapService,
        veileder: String,
        innvilget: Boolean,
        vararg roller: AdRolle,
    ) {
        Mockito.reset(ldapService)
        for (rolle in roller) {
            Mockito.`when`(ldapService.harTilgang(veileder, rolle)).thenReturn(innvilget)
        }
    }
}
