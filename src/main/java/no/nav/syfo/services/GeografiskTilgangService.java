package no.nav.syfo.services;

import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.domain.PersonInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class GeografiskTilgangService {

    private final LdapService ldapService;
    private final OrganisasjonRessursEnhetService organisasjonRessursEnhetService;
    private final OrganisasjonEnhetService organisasjonEnhetService;

    @Autowired
    public GeografiskTilgangService(LdapService ldapService, OrganisasjonRessursEnhetService organisasjonRessursEnhetService, OrganisasjonEnhetService organisasjonEnhetService) {
        this.ldapService = ldapService;
        this.organisasjonRessursEnhetService = organisasjonRessursEnhetService;
        this.organisasjonEnhetService = organisasjonEnhetService;
    }

    public boolean harGeografiskTilgang(String veilederId, PersonInfo personInfo) {
        if (harNasjonalTilgang(veilederId)) {
            return true;
        }
        final List<String> navKontorerForGT = organisasjonEnhetService.finnNAVKontorForGT(personInfo.getGeografiskTilknytning());
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
        List<String> veiledersOverordnedeEnheter = veiledersEnheter.stream()
                .map(organisasjonEnhetService::hentOverordnetEnhetForNAVKontor)
                .flatMap(Collection::stream)
                .collect(toList());

        return harRegionalTilgang(veilederId) && navKontorerForGT.stream()
                .map(organisasjonEnhetService::hentOverordnetEnhetForNAVKontor)
                .flatMap(Collection::stream)
                .anyMatch(veiledersOverordnedeEnheter::contains);
    }
}
