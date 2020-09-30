package no.nav.syfo.services;

import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.domain.PersonInfo;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.skjermedepersoner.SkjermedePersonerPipConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.CacheConfig.*;
import static no.nav.syfo.domain.AdRoller.*;

@Service
public class TilgangService {

    public static final String GEOGRAFISK = "GEOGRAFISK";

    private final AxsysConsumer axsysConsumer;
    private final LdapService ldapService;
    private final SkjermedePersonerPipConsumer skjermedePersonerPipConsumer;
    private final GeografiskTilgangService geografiskTilgangService;
    private final PersonService personService;

    private final static String ENHET = "ENHET";
    private final static String DISKRESJONSKODE_KODE6 = "SPSF";
    private final static String DISKRESJONSKODE_KODE7 = "SPFO";

    @Autowired
    public TilgangService(
            AxsysConsumer axsysConsumer,
            LdapService ldapService,
            SkjermedePersonerPipConsumer skjermedePersonerPipConsumer,
            GeografiskTilgangService geografiskTilgangService,
            PersonService personService
    ) {
        this.ldapService = ldapService;
        this.skjermedePersonerPipConsumer = skjermedePersonerPipConsumer;
        this.geografiskTilgangService = geografiskTilgangService;
        this.axsysConsumer = axsysConsumer;
        this.personService = personService;
    }

    public Tilgang sjekkTilgangTilBruker(String veilederId, String fnr) {
        PersonInfo personInfo = personService.hentPersonInfo(fnr);
        return sjekkTilgang(fnr, veilederId, personInfo);
    }

    @Cacheable(cacheNames = TILGANGTILBRUKER, key = "#veilederId.concat(#brukerFnr)", condition = "#brukerFnr != null && #veilederId != null")
    public Tilgang sjekkTilgang(String brukerFnr, String veilederId, PersonInfo personInfo) {
        if (!harTilgangTilTjenesten(veilederId)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(SYFO.name());
        }

        if (!geografiskTilgangService.harGeografiskTilgang(veilederId, brukerFnr, personInfo.getGeografiskTilknytning())) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(GEOGRAFISK);
        }

        String diskresjonskode = personInfo.getDiskresjonskode();
        if (DISKRESJONSKODE_KODE6.equals(diskresjonskode)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE6.name());
        } else if (DISKRESJONSKODE_KODE7.equals(diskresjonskode) && !harTilgangTilKode7(veilederId)) {
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE7.name());
        }

        if (skjermedePersonerPipConsumer.erSkjermet(brukerFnr) && !harTilgangTilEgenAnsatt(veilederId)) {
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
        if (!harTilgangTilEnhet(veilederId, enhet))
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

    private boolean harTilgangTilEnhet(String veilederId, String navEnhetId) {
        return axsysConsumer.enheter(veilederId)
                .stream()
                .anyMatch(enhet -> enhet.getEnhetId().equals(navEnhetId));
    }
}
