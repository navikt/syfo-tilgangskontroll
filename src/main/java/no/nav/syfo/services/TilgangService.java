package no.nav.syfo.services;

import no.nav.syfo.domain.Tilgang;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static no.nav.syfo.domain.AdRoller.*;

@Service
public class TilgangService {

    @Inject
    private LdapService ldapService;
    @Inject
    private DiskresjonskodeService diskresjonskodeService;
    @Inject
    private EgenAnsattService egenAnsattService;
    @Inject
    private GeografiskTilgangService geografiskTilgangService;

    // TODO fix @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public Tilgang sjekkTilgang(String brukerFnr, String veilederId) {
        if (!harTilgangTilSykefravaersoppfoelging(veilederId)) {
            return new Tilgang().harTilgang(false).begrunnelse("SYFO");
        }

        String diskresjonskode = diskresjonskodeService.diskresjonskode(brukerFnr);
        if ("6".equals(diskresjonskode)) {
            return new Tilgang().harTilgang(false).begrunnelse("KODE6");
        } else if ("7".equals(diskresjonskode) && !harTilgangTilKode7(veilederId)) {
            return new Tilgang().harTilgang(false).begrunnelse("KODE7");
        }

        if (egenAnsattService.erEgenAnsatt(brukerFnr) && !harTilgangTilEgenAnsatt(veilederId)) {
            return new Tilgang().harTilgang(false).begrunnelse("EGEN_ANSATT");
        }

        if (!geografiskTilgangService.harGeografiskTilgang(veilederId, brukerFnr)) {
            return new Tilgang().harTilgang(false).begrunnelse("GEOGRAFISK");
        }

        return new Tilgang().harTilgang(true);
    }

    // TODO fix @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public boolean harTilgangTilTjenesten(String veilederId) {
        return harTilgangTilSykefravaersoppfoelging(veilederId);
    }


    private boolean harTilgangTilSykefravaersoppfoelging(String veilederId) {
        return ldapService.harTilgang(veilederId, SYFO.rolle);
    }

    private boolean harTilgangTilKode7(String veilederId) {
        return ldapService.harTilgang(veilederId, KODE7.rolle);
    }

    private boolean harTilgangTilEgenAnsatt(String veilederId) {
        return ldapService.harTilgang(veilederId, EGEN_ANSATT.rolle);
    }
}
