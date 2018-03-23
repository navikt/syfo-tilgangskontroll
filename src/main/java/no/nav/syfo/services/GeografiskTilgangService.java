package no.nav.syfo.services;

import no.nav.syfo.domain.AdRoller;

import javax.inject.Inject;

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
        String geografiskTilknytning = personService.hentGeografiskTilknytning(fnr);
        List<String> veiledersEnheter = organisasjonRessursEnhetService.hentVeiledersEnheter();

        if (harLokalTilgangTilBrukersEnhet(geografiskTilknytning, veiledersEnheter)) {
            return true;
        }

        if (harRegionalTilgangTilBrukersEnhet(geografiskTilknytning, veiledersEnheter)) {
            return true;
        }

        return false;
    }

    private boolean harNasjonalTilgang() {
        return ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.NASJONAL.rolle)
                || ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.UTVIDBAR_TIL_NASJONAL.rolle);
    }

    private boolean harLokalTilgangTilBrukersEnhet(String geografiskTilknytning, List<String> veiledersEnheter) {
        return veiledersEnheter.stream().anyMatch(veiledersEnhet -> veiledersEnhet.equals(geografiskTilknytning));
    }

    private boolean harRegionalTilgang() {
        return ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.REGIONAL.rolle)
                || ldapService.harTilgang(getSubjectHandler().getUid(), AdRoller.UTVIDBAR_TIL_REGIONAL.rolle);
    }

    private boolean harRegionalTilgangTilBrukersEnhet(String geografiskTilknytning, List<String> veiledersEnheter) {
        List<String> overordneteEnheter = organisasjonEnhetService.hentOverordnetEnhetForNAVKontor(geografiskTilknytning);
        return harRegionalTilgang() && veiledersEnheter.stream()
                .anyMatch(overordneteEnheter::contains);
    }
}
