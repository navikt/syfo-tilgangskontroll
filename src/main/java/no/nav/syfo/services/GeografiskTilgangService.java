package no.nav.syfo.services;

import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.axsys.AxsysEnhet;
import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.domain.PersonInfo;
import no.nav.syfo.norg2.NorgConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class GeografiskTilgangService {

    private final AxsysConsumer axsysConsumer;
    private final LdapService ldapService;
    private final NorgConsumer norgConsumer;
    private final OrganisasjonEnhetService organisasjonEnhetService;

    @Autowired
    public GeografiskTilgangService(
            AxsysConsumer axsysConsumer,
            LdapService ldapService,
            NorgConsumer norgConsumer,
            OrganisasjonEnhetService organisasjonEnhetService
    ) {
        this.axsysConsumer = axsysConsumer;
        this.ldapService = ldapService;
        this.norgConsumer = norgConsumer;
        this.organisasjonEnhetService = organisasjonEnhetService;
    }

    public boolean harGeografiskTilgang(String veilederId, PersonInfo personInfo) {
        if (harNasjonalTilgang(veilederId)) {
            return true;
        }
        final String navKontorForGT = norgConsumer.getNAVKontorForGT(personInfo.getGeografiskTilknytning());
        final List<String> veiledersEnheter = axsysConsumer.enheter(veilederId)
                .stream()
                .map(AxsysEnhet::getEnhetId)
                .collect(toList());

        return harLokalTilgangTilBrukersEnhet(navKontorForGT, veiledersEnheter)
                || harRegionalTilgangTilBrukersEnhet(navKontorForGT, veiledersEnheter, veilederId);
    }

    private boolean harNasjonalTilgang(String veilederId) {
        return ldapService.harTilgang(veilederId, AdRoller.NASJONAL.rolle)
                || ldapService.harTilgang(veilederId, AdRoller.UTVIDBAR_TIL_NASJONAL.rolle);
    }

    private boolean harLokalTilgangTilBrukersEnhet(String navKontorForGT, List<String> veiledersEnheter) {
        return veiledersEnheter.contains(navKontorForGT);
    }

    private boolean harRegionalTilgang(String veilederId) {
        return ldapService.harTilgang(veilederId, AdRoller.REGIONAL.rolle)
                || ldapService.harTilgang(veilederId, AdRoller.UTVIDBAR_TIL_REGIONAL.rolle);
    }

    private boolean harRegionalTilgangTilBrukersEnhet(String navKontorForGT, List<String> veiledersEnheter, String veilederId) {
        List<String> veiledersOverordnedeEnheter = veiledersEnheter.stream()
                .map(organisasjonEnhetService::hentOverordnetEnhetForNAVKontor)
                .flatMap(Collection::stream)
                .collect(toList());

        return harRegionalTilgang(veilederId) && organisasjonEnhetService.hentOverordnetEnhetForNAVKontor(navKontorForGT)
                .stream()
                .anyMatch(veiledersOverordnedeEnheter::contains);
    }
}
