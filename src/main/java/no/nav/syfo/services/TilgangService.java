package no.nav.syfo.services;

import no.nav.syfo.domain.PersonInfo;
import no.nav.syfo.domain.Tilgang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static no.nav.syfo.domain.AdRoller.*;

@Service
public class TilgangService {

    public static final String GEOGRAFISK = "GEOGRAFISK";
    public static final String TILGANGTILBRUKER = "tilgangtilbruker";
    public static final String TILGANGTILTJENESTEN = "tilgangtiltjenesten";
    public static final String TILGANGTILENHET = "tilgangtilenhet";

    @Autowired
    private LdapService ldapService;
    @Autowired
    private EgenAnsattService egenAnsattService;
    @Autowired
    private GeografiskTilgangService geografiskTilgangService;
    @Autowired
    private OrganisasjonRessursEnhetService organisasjonRessursEnhetService;

    private final static String ENHET = "ENHET";
    private final static String DISKRESJONSKODE_KODE6 = "SPSF";
    private final static String DISKRESJONSKODE_KODE7 = "SPFO";

    @Cacheable(cacheNames = TILGANGTILBRUKER, key = "#veilederId.concat(#brukerFnr)", condition = "#brukerFnr != null && #veilederId != null")
    public Tilgang sjekkTilgang(String brukerFnr, String veilederId, PersonInfo personInfo) {
        if (!harTilgangTilTjenesten(veilederId)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(SYFO.name());
        }

        if (!geografiskTilgangService.harGeografiskTilgang(veilederId, personInfo)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(GEOGRAFISK);
        }

        String diskresjonskode = personInfo.getDiskresjonskode();
        if (DISKRESJONSKODE_KODE6.equals(diskresjonskode)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE6.name());
        } else if (DISKRESJONSKODE_KODE7.equals(diskresjonskode) && !harTilgangTilKode7(veilederId)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE7.name());
        }

        if (egenAnsattService.erEgenAnsatt(brukerFnr) && !harTilgangTilEgenAnsatt(veilederId)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(EGEN_ANSATT.name());
        }

        return new Tilgang().withHarTilgang(true);
    }

    @Cacheable(cacheNames = TILGANGTILTJENESTEN, key = "#veilederId", condition = "#veilederId != null")
    public Tilgang sjekkTilgangTilTjenesten(String veilederId) {
        if (harTilgangTilTjenesten(veilederId))
            return new Tilgang().withHarTilgang(true);
        return new Tilgang().withHarTilgang(false).withBegrunnelse(SYFO.name());
    }

    @Cacheable(cacheNames = TILGANGTILENHET, key = "#veilederId.concat(#enhet)", condition = "#enhet != null && #veilederId != null")
    public Tilgang sjekkTilgangTilEnhet(String veilederId, String enhet) {
        if (!harTilgangTilSykefravaersoppfoelging(veilederId))
            return new Tilgang().withHarTilgang(false).withBegrunnelse(SYFO.name());
        if (!organisasjonRessursEnhetService.harTilgangTilEnhet(veilederId, enhet))
            return new Tilgang().withHarTilgang(false).withBegrunnelse(ENHET);
        return new Tilgang().withHarTilgang(true);
    }

    private boolean harTilgangTilTjenesten(String veilederId) {
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
