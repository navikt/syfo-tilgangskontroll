package no.nav.syfo.services;

import no.nav.syfo.domain.PersonInfo;
import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;

import javax.xml.ws.soap.SOAPFaultException;

import static java.util.Optional.ofNullable;
import static no.nav.syfo.config.CacheConfig.CACHENAME_PERSON_INFO;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PersonService {

    private static final Logger log = getLogger(PersonService.class);

    private final PersonV3 personV3;

    @Autowired
    public PersonService(PersonV3 personV3) {
        this.personV3 = personV3;
    }

    @Retryable(
            value = {SOAPFaultException.class},
            backoff = @Backoff(delay = 200, maxDelay = 1000)
    )
    @Cacheable(cacheNames = CACHENAME_PERSON_INFO, key = "#fnr", condition = "#fnr != null")
    public PersonInfo hentPersonInfo(String fnr) {
        try {
            HentGeografiskTilknytningResponse geografiskTilknytningResponse = personV3.hentGeografiskTilknytning(new HentGeografiskTilknytningRequest().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(fnr))));
            String geografiskTilknytning = ofNullable(geografiskTilknytningResponse.getGeografiskTilknytning()).map(GeografiskTilknytning::getGeografiskTilknytning).orElse("");
            if (geografiskTilknytningResponse.getDiskresjonskode() != null && geografiskTilknytningResponse.getDiskresjonskode().getValue() != null) {
                String diskresjonskode = geografiskTilknytningResponse.getDiskresjonskode().getValue();
                return new PersonInfo(diskresjonskode, geografiskTilknytning);
            } else {
                return new PersonInfo("", geografiskTilknytning);
            }
        } catch (HentGeografiskTilknytningSikkerhetsbegrensing | HentGeografiskTilknytningPersonIkkeFunnet e) {
            log.error("Feil ved henting av geografisk tilknytning", e);
            throw new RuntimeException("Feil ved henting av geografisk tilknytning", e);
        } catch (Exception e) {
            log.error("Henting av geografisk tilknytning fra personV3 feilet pga en uventet exception", e);
            if (e instanceof SOAPFaultException) {
                throw e;
            } else {
                throw new RuntimeException("Fikk en uventet exception ved henting av geografisk tilknytning", e);
            }
        }
    }

    @Recover
    public void recover(SOAPFaultException e) {
        log.error("Feil ved henting av geografisk tilknytning etter maks antall kall", e);
        throw e;
    }
}
