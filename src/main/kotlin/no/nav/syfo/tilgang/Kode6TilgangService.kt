package no.nav.syfo.tilgang

import io.micrometer.core.annotation.Timed
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.domain.AdRoller
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Kode6TilgangService(
    private val adRoller: AdRoller,
    @Value("\${fastlegerest.client.id}") private val fastlegerestClientId: String,
    @Value("\${finnfastlege.client.id}") private val finnfastlegeClientId: String,
    @Value("\${isdialogmelding.client.id}") private val isdialogmeldingClientId: String,
    @Value("\${isdialogmote.client.id}") private val isdialogmoteClientId: String,
    @Value("\${isdialogmotekandidat.client.id}") private val isdialogmotekandidatClientId: String,
    @Value("\${isnarmesteleder.client.id}") private val isnarmestelederClientId: String,
    @Value("\${isoppfolgingstilfelle.client.id}") private val isoppfolgingstilfelleClientId: String,
    @Value("\${ispengestopp.client.id}") private val ispengestoppClientId: String,
    @Value("\${ispersonoppgave.client.id}") private val ispersonoppgaveClientId: String,
    @Value("\${smregistrering.client.id}") private val smregisteringClientId: String,
    @Value("\${syfobehandlendeenhet.client.id}") private val syfobehandlendeenhetClientId: String,
    @Value("\${syfomodiaperson.client.id}") private val syfomodiapersonClientId: String,
    @Value("\${syfooversiktsrv.client.id}") private val syfooversiktsrvClientId: String,
    @Value("\${syfoperson.client.id}") private val syfopersonClientId: String,
    private val graphApiConsumer: GraphApiConsumer,
) {
    private var kode6TjenesteList = listOf(
        fastlegerestClientId,
        finnfastlegeClientId,
        isdialogmeldingClientId,
        isdialogmoteClientId,
        isdialogmotekandidatClientId,
        isnarmestelederClientId,
        isoppfolgingstilfelleClientId,
        ispengestoppClientId,
        ispersonoppgaveClientId,
        smregisteringClientId,
        syfobehandlendeenhetClientId,
        syfomodiapersonClientId,
        syfooversiktsrvClientId,
        syfopersonClientId,
    )

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
