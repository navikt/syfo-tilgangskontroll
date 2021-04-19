package no.nav.syfo.tilgang

import no.nav.syfo.consumer.ldap.LdapService
import no.nav.syfo.domain.AdRoller
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Kode6TilgangService(
    @Value("\${smregistrering.client.id}") private val smregisteringClientId: String,
    private val ldapService: LdapService,
) {
    private var kode6TjenesteList = listOf(smregisteringClientId)

    fun harTilgang(
        consumerClientId: String,
        veilederId: String
    ): Boolean {
        return harTjenesteTilgang(consumerClientId) && harVeilederTilgang(veilederId)
    }

    private fun harTjenesteTilgang(consumerClientId: String): Boolean {
        return kode6TjenesteList.contains(consumerClientId)
    }

    private fun harVeilederTilgang(veilederId: String): Boolean {
        return ldapService.harTilgang(veilederId, AdRoller.KODE6.rolle)
    }
}
