package no.nav.syfo.tilgang

import io.micrometer.core.annotation.Timed
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.domain.AdRoller
import org.springframework.stereotype.Service

@Service
class Kode6TilgangService(
    private val adRoller: AdRoller,
    private val graphApiConsumer: GraphApiConsumer,
) {

    @Timed("syfotilgangskontroll_kode6TilgangSevice_harTilgang", histogram = true)
    fun harTilgang(
        veilederId: String
    ): Boolean {
        return graphApiConsumer.hasAccess(veilederId, adRoller.KODE6)
    }
}
