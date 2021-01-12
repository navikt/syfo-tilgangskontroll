package no.nav.syfo.tilgang

import no.nav.syfo.axsys.AxsysConsumer
import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.geografisktilknytning.GeografiskTilgangService
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.services.LdapService
import no.nav.syfo.skjermedepersoner.SkjermedePersonerPipConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TilgangService @Autowired constructor(
    private val axsysConsumer: AxsysConsumer,
    private val ldapService: LdapService,
    private val skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer,
    private val geografiskTilgangService: GeografiskTilgangService,
    private val pdlConsumer: PdlConsumer
) {
    fun sjekkTilgangTilBruker(veilederId: String, fnr: String): Tilgang {
        return sjekkTilgang(fnr, veilederId)
    }

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILBRUKER], key = "#veilederId.concat(#brukerFnr)", condition = "#brukerFnr != null && #veilederId != null")
    fun sjekkTilgang(brukerFnr: String, veilederId: String): Tilgang {
        if (!harTilgangTilTjenesten(veilederId)) {
            return Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.SYFO.name)
        }
        if (!geografiskTilgangService.harGeografiskTilgang(veilederId, brukerFnr)) {
            return Tilgang().withHarTilgang(false).withBegrunnelse(GEOGRAFISK)
        }
        val personIdentNumber = PersonIdentNumber(brukerFnr)
        val pdlPerson = pdlConsumer.person(personIdentNumber.value)
        if (pdlConsumer.isKode6(pdlPerson)) {
            return Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.KODE6.name)
        } else if (pdlConsumer.isKode7(pdlPerson) && !harTilgangTilKode7(veilederId)) {
            return Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.KODE7.name)
        }
        return if (skjermedePersonerPipConsumer.erSkjermet(brukerFnr) && !harTilgangTilEgenAnsatt(veilederId)) {
            Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.EGEN_ANSATT.name)
        } else Tilgang().withHarTilgang(true)
    }

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILTJENESTEN], key = "#veilederId", condition = "#veilederId != null")
    fun sjekkTilgangTilTjenesten(veilederId: String?): Tilgang {
        return if (harTilgangTilTjenesten(veilederId)) Tilgang().withHarTilgang(true) else Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.SYFO.name)
    }

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILENHET], key = "#veilederId.concat(#enhet)", condition = "#enhet != null && #veilederId != null")
    fun sjekkTilgangTilEnhet(veilederId: String, enhet: String): Tilgang {
        if (!harTilgangTilSykefravaersoppfoelging(veilederId)) return Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.SYFO.name)
        return if (!harTilgangTilEnhet(veilederId, enhet)) Tilgang().withHarTilgang(false).withBegrunnelse(ENHET) else Tilgang().withHarTilgang(true)
    }

    private fun harTilgangTilTjenesten(veilederId: String?): Boolean {
        return harTilgangTilSykefravaersoppfoelging(veilederId)
    }

    private fun harTilgangTilSykefravaersoppfoelging(veilederId: String?): Boolean {
        return ldapService.harTilgang(veilederId, AdRoller.SYFO.rolle)
    }

    private fun harTilgangTilKode7(veilederId: String?): Boolean {
        return ldapService.harTilgang(veilederId, AdRoller.KODE7.rolle)
    }

    private fun harTilgangTilEgenAnsatt(veilederId: String): Boolean {
        return ldapService.harTilgang(veilederId, AdRoller.EGEN_ANSATT.rolle)
    }

    private fun harTilgangTilEnhet(veilederId: String, navEnhetId: String): Boolean {
        return axsysConsumer.enheter(veilederId)
            .stream()
            .anyMatch { (enhetId) -> enhetId == navEnhetId }
    }

    companion object {
        const val GEOGRAFISK = "GEOGRAFISK"
        private const val ENHET = "ENHET"
    }
}
