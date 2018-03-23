package no.nav.syfo.services;

import no.nav.syfo.domain.Tilgang;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

import static no.nav.brukerdialog.security.context.SubjectHandler.getSubjectHandler;
import static no.nav.syfo.domain.AdRoller.*;

public class TilgangService {

    @Inject
    private LdapService ldapService;
    @Inject
    private DiskresjonskodeService diskresjonskodeService;
    @Inject
    private EgenAnsattService egenAnsattService;
    @Inject
    private GeografiskTilgangService geografiskTilgangService;

    @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public Tilgang sjekkTilgang(String fnr) {
        if (!harTilgangTilSykefravaersoppfoelging()) {
            return new Tilgang().harTilgang(false).begrunnelse("SYFO");
        }

        String diskresjonskode = diskresjonskodeService.diskresjonskode(fnr);
        if ("6".equals(diskresjonskode)) {
            return new Tilgang().harTilgang(false).begrunnelse("KODE6");
        } else if ("7".equals(diskresjonskode) && !harTilgangTilKode7()) {
            return new Tilgang().harTilgang(false).begrunnelse("KODE7");
        }

        if (egenAnsattService.erEgenAnsatt(fnr) && !harTilgangTilEgenAnsatt()) {
            return new Tilgang().harTilgang(false).begrunnelse("EGEN_ANSATT");
        }

        if (!geografiskTilgangService.harGeografiskTilgang(fnr)) {
            return new Tilgang().harTilgang(false).begrunnelse("GEOGRAFISK");
        }

        return new Tilgang().harTilgang(true);
    }

    @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public boolean harTilgangTilTjenesten() {
        return harTilgangTilSykefravaersoppfoelging();
    }


    private boolean harTilgangTilSykefravaersoppfoelging() {
        return ldapService.harTilgang(getSubjectHandler().getUid(), SYFO.rolle);
    }

    private boolean harTilgangTilKode7() {
        return ldapService.harTilgang(getSubjectHandler().getUid(), KODE7.rolle);
    }

    private boolean harTilgangTilEgenAnsatt() {
        return ldapService.harTilgang(getSubjectHandler().getUid(), EGEN_ANSATT.rolle);
    }
}
