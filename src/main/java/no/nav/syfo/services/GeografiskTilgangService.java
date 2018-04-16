package no.nav.syfo.services;

import no.nav.syfo.domain.AdRoller;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static no.nav.brukerdialog.security.context.SubjectHandler.getSubjectHandler;

public class GeografiskTilgangService {

    @Inject
    private LdapService ldapService;
    @Inject
    private PersonService personService;
    @Inject
    private OrganisasjonRessursEnhetService organisasjonRessursEnhetService;
    @Inject
    private OrganisasjonEnhetService organisasjonEnhetService;

    public boolean harGeografiskTilgang(String fnr) {
        if (harNasjonalTilgang()) {
            return true;
        }
        final String geografiskTilknytning = personService.hentGeografiskTilknytning(fnr);
        final List<String> navKontorerForGT = organisasjonEnhetService.finnNAVKontorForGT(geografiskTilknytning);
        final List<String> veiledersEnheter = organisasjonRessursEnhetService.hentVeiledersEnheter();

        return harLokalTilgangTilBrukersEnhet(navKontorerForGT, veiledersEnheter)
                || harRegionalTilgangTilBrukersEnhet(navKontorerForGT, veiledersEnheter);
    }

    private boolean harNasjonalTilgang() {
        return ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.NASJONAL.rolle)
                || ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.UTVIDBAR_TIL_NASJONAL.rolle);
    }

    private boolean harLokalTilgangTilBrukersEnhet(List<String> navKontorerForGT, List<String> veiledersEnheter) {
        return navKontorerForGT.stream().anyMatch(veiledersEnheter::contains);
    }

    private boolean harRegionalTilgang() {
        return ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.REGIONAL.rolle)
                || ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.UTVIDBAR_TIL_REGIONAL.rolle);
    }

    private boolean harRegionalTilgangTilBrukersEnhet(List<String> navKontorerForGT, List<String> veiledersEnheter) {
        return harRegionalTilgang() && navKontorerForGT.stream()
                .map(organisasjonEnhetService::hentOverordnetEnhetForNAVKontor)
                .flatMap(Collection::stream)
                .anyMatch(veiledersEnheter::contains);
    }
}
