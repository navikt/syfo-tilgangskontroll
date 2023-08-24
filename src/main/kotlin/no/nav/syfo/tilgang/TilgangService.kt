package no.nav.syfo.tilgang

import io.micrometer.core.annotation.Timed
import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.skjermedepersoner.SkjermedePersonerPipConsumer
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.geografisktilknytning.GeografiskTilgangService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TilgangService @Autowired constructor(
    private val adRoller: AdRoller,
    private val axsysConsumer: AxsysConsumer,
    private val papirsykmeldingTilgangService: PapirsykmeldingTilgangService,
    private val graphApiConsumer: GraphApiConsumer,
    private val skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer,
    private val geografiskTilgangService: GeografiskTilgangService,
    private val pdlConsumer: PdlConsumer
) {
    fun sjekkTilgangTilBruker(
        veilederId: String,
        fnr: String,
        consumerClientId: String = ""
    ): Tilgang {
        return sjekkTilgang(fnr, veilederId, consumerClientId)
    }

    @Cacheable(
        cacheNames = [CacheConfig.TILGANGTILBRUKER],
        key = "#veilederId.concat(#brukerFnr).concat(#consumerClientId)",
        condition = "#brukerFnr != null && #veilederId != null && #consumerClientId != null"
    )
    fun sjekkTilgang(
        brukerFnr: String,
        veilederId: String,
        consumerClientId: String
    ): Tilgang {
        if (!harTilgangTilTjenesten(veilederId)) {
            return Tilgang(
                harTilgang = false,
            )
        }
        if (!geografiskTilgangService.harGeografiskTilgang(veilederId, brukerFnr)) {
            return Tilgang(
                harTilgang = false,
            )
        }
        val personIdentNumber = PersonIdentNumber(brukerFnr)
        val pdlPerson = pdlConsumer.person(personIdentNumber.value)
        if (pdlConsumer.isKode6(pdlPerson) && !harTilgangTilKode6(veilederId)) {
            return Tilgang(
                harTilgang = false,
            )
        } else if (pdlConsumer.isKode7(pdlPerson) && !harTilgangTilKode7(veilederId)) {
            return Tilgang(
                harTilgang = false,
            )
        }
        return if (skjermedePersonerPipConsumer.erSkjermet(brukerFnr) && !harTilgangTilEgenAnsatt(veilederId)) {
            return Tilgang(
                harTilgang = false,
            )
        } else Tilgang(harTilgang = true)
    }

    @Cacheable(
        cacheNames = [CacheConfig.TILGANGTILBRUKER_PAPIRSYKMELDING],
        key = "#veilederIdent.concat(#personIdent).concat(#consumerClientId)",
        condition = "#personIdent != null && #veilederIdent != null && #consumerClientId !=null"
    )
    fun sjekkTilgangTilBrukerMedPapirsykmelding(
        consumerClientId: String,
        personIdent: String,
        veilederIdent: String,
    ): Tilgang {
        return if (papirsykmeldingTilgangService.harPapirsykmeldingTilgang(veilederIdent = veilederIdent)) {
            sjekkTilgang(
                consumerClientId = consumerClientId,
                brukerFnr = personIdent,
                veilederId = veilederIdent,
            )
        } else Tilgang(
            harTilgang = false,
        )
    }

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILTJENESTEN], key = "#veilederId", condition = "#veilederId != null")
    fun sjekkTilgangTilTjenesten(veilederId: String): Tilgang {
        return if (harTilgangTilTjenesten(veilederId)) Tilgang(
            harTilgang = true
        ) else Tilgang(
            harTilgang = false,
        )
    }

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILENHET], key = "#veilederId.concat(#enhet)", condition = "#enhet != null && #veilederId != null")
    fun sjekkTilgangTilEnhet(veilederId: String, enhet: String): Tilgang {
        if (!harTilgangTilSykefravaersoppfoelging(veilederId)) return Tilgang(
            harTilgang = false,
        )
        return if (!harTilgangTilEnhet(veilederId, enhet)) Tilgang(
            harTilgang = false,
        ) else Tilgang(
            harTilgang = true
        )
    }

    fun preloadCacheForBrukere(personIdentList: List<String>) {
        personIdentList.forEach { personIdent ->
            try {
                pdlConsumer.person(personIdent)
                pdlConsumer.geografiskTilknytningResponse(personIdent)
                skjermedePersonerPipConsumer.erSkjermet(personIdent, systemToken = true)
            } catch (exc: Exception) {
                log.warn("Failed to preload cache", exc)
            }
        }
    }

    @Timed("syfotilgangskontroll_harTilgangTilTjenesten", histogram = true)
    private fun harTilgangTilTjenesten(veilederId: String): Boolean {
        return harTilgangTilSykefravaersoppfoelging(veilederId)
    }

    private fun harTilgangTilSykefravaersoppfoelging(veilederId: String): Boolean {
        return graphApiConsumer.hasAccess(veilederId, adRoller.SYFO)
    }

    @Timed("syfotilgangskontroll_kode6TilgangSevice_harTilgang", histogram = true)
    fun harTilgangTilKode6(veilederId: String): Boolean {
        return graphApiConsumer.hasAccess(veilederId, adRoller.KODE6)
    }

    @Timed("syfotilgangskontroll_harTilgangTilKode7", histogram = true)
    private fun harTilgangTilKode7(veilederId: String): Boolean {
        return graphApiConsumer.hasAccess(veilederId, adRoller.KODE7)
    }

    @Timed("syfotilgangskontroll_harTilgangTilEgenAnsatt", histogram = true)
    private fun harTilgangTilEgenAnsatt(veilederId: String): Boolean {
        return graphApiConsumer.hasAccess(veilederId, adRoller.EGEN_ANSATT)
    }

    private fun harTilgangTilEnhet(veilederId: String, navEnhetId: String): Boolean {
        val veiledersEnheter = axsysConsumer.enheter(veilederId)
        val veilederHasAccessToEnhet = veiledersEnheter
            .stream()
            .anyMatch { (enhetId) -> enhetId == navEnhetId }

        if (!veilederHasAccessToEnhet) {
            val veiledersEnheterAsString = veiledersEnheter.joinToString(", ")
            log.info("Veileder har ikke tilgang til ønsket enhet! Ønsket enhet: $navEnhetId, veileders enheter: $veiledersEnheterAsString")
            return false
        }
        return true
    }

    companion object {
        const val GEOGRAFISK = "GEOGRAFISK"
        const val ENHET = "ENHET"
        private val log = LoggerFactory.getLogger(TilgangService::class.java)
    }
}
