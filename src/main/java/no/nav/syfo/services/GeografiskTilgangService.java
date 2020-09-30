package no.nav.syfo.services;

import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.axsys.AxsysEnhet;
import no.nav.syfo.behandlendeenhet.BehandlendeEnhetConsumer;
import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.norg2.NorgConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class GeografiskTilgangService {

    private final AxsysConsumer axsysConsumer;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private final LdapService ldapService;
    private final NorgConsumer norgConsumer;

    @Autowired
    public GeografiskTilgangService(
            AxsysConsumer axsysConsumer,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            LdapService ldapService,
            NorgConsumer norgConsumer
    ) {
        this.axsysConsumer = axsysConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.ldapService = ldapService;
        this.norgConsumer = norgConsumer;
    }

    public boolean harGeografiskTilgang(String veilederId, String personFnr, String geografiskTilknytning) {
        if (harNasjonalTilgang(veilederId)) {
            return true;
        }
        final String navKontorForGT = getNavKontorForGT(personFnr, geografiskTilknytning);
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
                .map(norgConsumer::getOverordnetEnhetListForNAVKontor)
                .flatMap(Collection::stream)
                .collect(toList());

        return harRegionalTilgang(veilederId) && norgConsumer.getOverordnetEnhetListForNAVKontor(navKontorForGT)
                .stream()
                .anyMatch(veiledersOverordnedeEnheter::contains);
    }

    private String getNavKontorForGT(String personFnr, String geografiskTilknytning) {
        return isGeografiskTilknytningUtland(geografiskTilknytning)
                ? behandlendeEnhetConsumer.getBehandlendeEnhet(personFnr, null).getEnhetId()
                : norgConsumer.getNAVKontorForGT(geografiskTilknytning);
    }

    private boolean isGeografiskTilknytningUtland(String geografiskTilknytning) {
        return geografiskTilknytning.matches("[a-zA-Z]{3}");
    }
}
