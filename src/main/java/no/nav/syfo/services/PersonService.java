package no.nav.syfo.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.PersonInfo;
import no.nav.tjeneste.virksomhet.person.v3.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSGeografiskTilknytning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentGeografiskTilknytningResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class PersonService {

    @Autowired
    private PersonV3 personV3;

    public PersonInfo hentPersonInfo(String fnr) {
        try {
            WSHentGeografiskTilknytningResponse geografiskTilknytningResponse = personV3.hentGeografiskTilknytning(new WSHentGeografiskTilknytningRequest().withAktoer(new WSPersonIdent().withIdent(new WSNorskIdent().withIdent(fnr))));
            String geografiskTilknytning = ofNullable(geografiskTilknytningResponse.getGeografiskTilknytning()).map(WSGeografiskTilknytning::getGeografiskTilknytning).orElse("");
            if(geografiskTilknytningResponse.getDiskresjonskode() != null && geografiskTilknytningResponse.getDiskresjonskode().getValue() != null){
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
            throw new RuntimeException("Fikk en uventet exception ved henting av geografisk tilknytning", e);
        }
    }
}
