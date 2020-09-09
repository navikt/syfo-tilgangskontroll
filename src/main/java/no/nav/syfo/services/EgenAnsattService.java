package no.nav.syfo.services;

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;
import javax.xml.ws.soap.SOAPFaultException;

import static no.nav.syfo.config.CacheConfig.CACHENAME_EGENANSATT;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class EgenAnsattService {

    private static final Logger log = getLogger(PersonService.class);

    private final EgenAnsattV1 egenAnsattV1;

    @Autowired
    public EgenAnsattService(EgenAnsattV1 egenAnsattV1) {
        this.egenAnsattV1 = egenAnsattV1;
    }

    @Retryable(
            value = {SOAPFaultException.class},
            backoff = @Backoff(delay = 200, maxDelay = 1000)
    )
    @Cacheable(cacheNames = CACHENAME_EGENANSATT, key = "#fnr", condition = "#fnr != null")
    public boolean erEgenAnsatt(String fnr) {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt();
    }

    @Recover
    public void recover(SOAPFaultException e) {
        log.error("Feil ved kall hentErEgenAnsattEllerIFamilieMedEgenAnsatt etter maks antall kall", e);
        throw e;
    }
}
