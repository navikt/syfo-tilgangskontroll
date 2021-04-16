package no.nav.syfo.tilgang

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.ldap.LdapService
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.skjermedepersoner.SkjermedePersonerPipConsumer
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.geografisktilknytning.GeografiskTilgangService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TilgangService @Autowired constructor(
    private val axsysConsumer: AxsysConsumer,
    private val kode6TilgangService: Kode6TilgangService,
    private val ldapService: LdapService,
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

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILBRUKER], key = "#veilederId.concat(#brukerFnr)", condition = "#brukerFnr != null && #veilederId != null")
    fun sjekkTilgang(
        brukerFnr: String,
        veilederId: String,
        consumerClientId: String
    ): Tilgang {
        if (!harTilgangTilTjenesten(veilederId)) {
            return Tilgang(
                harTilgang = false,
                begrunnelse = AdRoller.SYFO.name
            )
        }
        if (!geografiskTilgangService.harGeografiskTilgang(veilederId, brukerFnr)) {
            return Tilgang(
                harTilgang = false,
                begrunnelse = GEOGRAFISK
            )
        }
        val personIdentNumber = PersonIdentNumber(brukerFnr)
        val pdlPerson = pdlConsumer.person(personIdentNumber.value)
        if (
            pdlConsumer.isKode6(pdlPerson)
            && !kode6TilgangService.harTilgang(
                consumerClientId = consumerClientId,
                veilederId = veilederId
            )
        ) {
            return Tilgang(
                harTilgang = false,
                begrunnelse = AdRoller.KODE6.name
            )
        } else if (pdlConsumer.isKode7(pdlPerson) && !harTilgangTilKode7(veilederId)) {
            return Tilgang(
                harTilgang = false,
                begrunnelse = AdRoller.KODE7.name
            )
        }
        return if (skjermedePersonerPipConsumer.erSkjermet(brukerFnr) && !harTilgangTilEgenAnsatt(veilederId)) {
            return Tilgang(
                harTilgang = false,
                begrunnelse = AdRoller.EGEN_ANSATT.name
            )
        } else Tilgang(harTilgang = true)
    }

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILTJENESTEN], key = "#veilederId", condition = "#veilederId != null")
    fun sjekkTilgangTilTjenesten(veilederId: String): Tilgang {
        return if (harTilgangTilTjenesten(veilederId)) Tilgang(
            harTilgang = true
        ) else Tilgang(
            harTilgang = false,
            begrunnelse = AdRoller.SYFO.name
        )
    }

    @Cacheable(cacheNames = [CacheConfig.TILGANGTILENHET], key = "#veilederId.concat(#enhet)", condition = "#enhet != null && #veilederId != null")
    fun sjekkTilgangTilEnhet(veilederId: String, enhet: String): Tilgang {
        if (!harTilgangTilSykefravaersoppfoelging(veilederId)) return Tilgang(
            harTilgang = false,
            begrunnelse = AdRoller.SYFO.name
        )
        return if (!harTilgangTilEnhet(veilederId, enhet)) Tilgang(
            harTilgang = false,
            begrunnelse = ENHET
        ) else Tilgang(
            harTilgang = true
        )
    }

    private fun harTilgangTilTjenesten(veilederId: String): Boolean {
        return harTilgangTilSykefravaersoppfoelging(veilederId)
    }

    private fun harTilgangTilSykefravaersoppfoelging(veilederId: String): Boolean {
        return ldapService.harTilgang(veilederId, AdRoller.SYFO.rolle)
    }

    private fun harTilgangTilKode7(veilederId: String): Boolean {
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
