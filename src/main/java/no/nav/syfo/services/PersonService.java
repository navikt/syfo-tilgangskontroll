package no.nav.syfo.services;

import no.nav.tjeneste.virksomhet.person.v3.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSGeografiskTilknytning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentGeografiskTilknytningResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PersonService {
    private static final Logger LOG = getLogger(PersonService.class);

    @Inject
    private PersonV3 personV3;

    public String hentGeografiskTilknytning(String fnr) {
        try {
            return of(personV3.hentGeografiskTilknytning(
                    new WSHentGeografiskTilknytningRequest()
                            .withAktoer(new WSPersonIdent().withIdent(new WSNorskIdent().withIdent(fnr)))))
                    .map(WSHentGeografiskTilknytningResponse::getGeografiskTilknytning)
                    .map(WSGeografiskTilknytning::getGeografiskTilknytning)
                    .orElse(null);
        } catch (HentGeografiskTilknytningSikkerhetsbegrensing | HentGeografiskTilknytningPersonIkkeFunnet e) {
            LOG.error("Feil ved henting av geografisk tilknytning", e);
            throw new RuntimeException("Feil ved henting av geografisk tilknytning", e);
        }
    }
}
