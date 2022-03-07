package no.nav.syfo.tilgang

import io.micrometer.core.annotation.Timed
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.domain.AdRoller
import org.springframework.stereotype.Service

@Service
class PapirsykmeldingTilgangService(
    private val adRoller: AdRoller,
    private val apiConsumerAccessService: APIConsumerAccessService,
    private val graphApiConsumer: GraphApiConsumer,
) {
    @Timed("syfotilgangskontroll_papirsykmelding_harTilgang", histogram = true)
    fun harPapirsykmeldingTilgang(
        veilederIdent: String,
    ): Boolean {
        val isConsumerApplicationAuthorized = apiConsumerAccessService.isConsumerApplicationAZPAuthorized(
            authorizedApplicationNameList = authorizedConsumerApplicationNameList,
        )
        val isConsumerVeilederIdentAuthorized = graphApiConsumer.hasAccess(
            adRolle = adRoller.PAPIRSYKMELDING,
            veilederIdent = veilederIdent,
        )
        return isConsumerApplicationAuthorized && isConsumerVeilederIdentAuthorized
    }

    companion object {
        private const val SMREGISTRERING_BACKEND_NAME = "smregistrering-backend"
        val authorizedConsumerApplicationNameList = listOf(
            SMREGISTRERING_BACKEND_NAME,
        )
    }
}
