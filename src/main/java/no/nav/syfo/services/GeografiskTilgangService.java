package no.nav.syfo.services;

import no.nav.syfo.domain.AdRoller;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@Service
public class GeografiskTilgangService {

    @Inject
    private LdapService ldapService;
    @Inject
    private PersonService personService;
    @Inject
    private OrganisasjonRessursEnhetService organisasjonRessursEnhetService;
    @Inject
    private OrganisasjonEnhetService organisasjonEnhetService;

    public boolean harGeografiskTilgang(String veilederId, String brukerFnr) {
        if (harNasjonalTilgang(veilederId)) {
            return true;
        }
        final String geografiskTilknytning = personService.hentGeografiskTilknytning(brukerFnr);
        final List<String> navKontorerForGT = organisasjonEnhetService.finnNAVKontorForGT(geografiskTilknytning);
        final List<String> veiledersEnheter = organisasjonRessursEnhetService.hentVeiledersEnheter(veilederId);

        return harLokalTilgangTilBrukersEnhet(navKontorerForGT, veiledersEnheter)
                || harRegionalTilgangTilBrukersEnhet(navKontorerForGT, veiledersEnheter, veilederId);
    }

    private boolean harNasjonalTilgang(String veilederId) {
        return ldapService.harTilgang(veilederId, AdRoller.NASJONAL.rolle)
                || ldapService.harTilgang(veilederId, AdRoller.UTVIDBAR_TIL_NASJONAL.rolle);
    }

    private boolean harLokalTilgangTilBrukersEnhet(List<String> navKontorerForGT, List<String> veiledersEnheter) {
        return navKontorerForGT.stream().anyMatch(veiledersEnheter::contains);
    }

    private boolean harRegionalTilgang(String veilederId) {
        return ldapService.harTilgang(veilederId, AdRoller.REGIONAL.rolle)
                || ldapService.harTilgang(veilederId, AdRoller.UTVIDBAR_TIL_REGIONAL.rolle);
    }

    private boolean harRegionalTilgangTilBrukersEnhet(List<String> navKontorerForGT, List<String> veiledersEnheter, String veilederId) {
        return harRegionalTilgang(veilederId) && navKontorerForGT.stream()
                .map(organisasjonEnhetService::hentOverordnetEnhetForNAVKontor)
                .flatMap(Collection::stream)
                .anyMatch(veiledersEnheter::contains);
    }
}
