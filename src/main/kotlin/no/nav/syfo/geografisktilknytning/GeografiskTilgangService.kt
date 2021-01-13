package no.nav.syfo.geografisktilknytning

import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.axsys.AxsysEnhet
import no.nav.syfo.consumer.behandlendeenhet.BehandlendeEnhetConsumer
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.consumer.norg2.NorgConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.ldap.LdapService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class GeografiskTilgangService @Autowired constructor(
    private val axsysConsumer: AxsysConsumer,
    private val behandlendeEnhetConsumer: BehandlendeEnhetConsumer,
    private val ldapService: LdapService,
    private val norgConsumer: NorgConsumer,
    private val pdlConsumer: PdlConsumer
) {
    fun harGeografiskTilgang(veilederId: String, personFnr: String): Boolean {
        if (harNasjonalTilgang(veilederId)) {
            return true
        }
        val geografiskTilknytning = pdlConsumer.geografiskTilknytning(personFnr)
        val navKontorForGT = getNavKontorForGT(personFnr, geografiskTilknytning)
        val veiledersEnheter = axsysConsumer.enheter(veilederId)
            .stream()
            .map(AxsysEnhet::enhetId)
            .collect(Collectors.toList())
        return (harLokalTilgangTilBrukersEnhet(navKontorForGT, veiledersEnheter)
            || harRegionalTilgangTilBrukersEnhet(navKontorForGT, veiledersEnheter, veilederId))
    }

    private fun harNasjonalTilgang(veilederId: String): Boolean {
        return (ldapService.harTilgang(veilederId, AdRoller.NASJONAL.rolle)
            || ldapService.harTilgang(veilederId, AdRoller.UTVIDBAR_TIL_NASJONAL.rolle))
    }

    private fun harLokalTilgangTilBrukersEnhet(navKontorForGT: String, veiledersEnheter: List<String>): Boolean {
        return veiledersEnheter.contains(navKontorForGT)
    }

    private fun harRegionalTilgang(veilederId: String): Boolean {
        return (ldapService.harTilgang(veilederId, AdRoller.REGIONAL.rolle)
            || ldapService.harTilgang(veilederId, AdRoller.UTVIDBAR_TIL_REGIONAL.rolle))
    }

    private fun harRegionalTilgangTilBrukersEnhet(navKontorForGT: String, veiledersEnheter: List<String>, veilederId: String): Boolean {
        val veiledersOverordnedeEnheter = veiledersEnheter.map { enhetNr: String ->
            norgConsumer.getOverordnetEnhetListForNAVKontor(enhetNr)
        }.flatten()

        return harRegionalTilgang(veilederId) && norgConsumer.getOverordnetEnhetListForNAVKontor(navKontorForGT)
            .any { overordnetEnhet: String -> veiledersOverordnedeEnheter.contains(overordnetEnhet) }
    }

    private fun getNavKontorForGT(personFnr: String, geografiskTilknytning: String): String {
        return if (isGeografiskTilknytningUtland(geografiskTilknytning)) behandlendeEnhetConsumer.getBehandlendeEnhet(personFnr, null).enhetId else norgConsumer.getNAVKontorForGT(geografiskTilknytning)
    }

    private fun isGeografiskTilknytningUtland(geografiskTilknytning: String): Boolean {
        return geografiskTilknytning.matches(Regex("[a-zA-Z]{3}"))
    }
}
