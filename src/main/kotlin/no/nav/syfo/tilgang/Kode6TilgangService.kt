package no.nav.syfo.tilgang

import io.micrometer.core.annotation.Timed
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.domain.AdRoller
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Kode6TilgangService(
    private val adRoller: AdRoller,
    @Value("\${smregistrering.client.id}") private val smregisteringClientId: String,
    private val graphApiConsumer: GraphApiConsumer,
) {
    private var kode6TjenesteList = listOf(smregisteringClientId)

    @Timed("syfotilgangskontroll_kode6TilgangSevice_harTilgang", histogram = true)
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
        return graphApiConsumer.hasAccess(veilederId, adRoller.KODE6)
    }
}
