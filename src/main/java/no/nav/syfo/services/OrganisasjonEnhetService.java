package no.nav.syfo.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.FinnNAVKontorUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetRelasjonstyper;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetsstatus;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSGeografi;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSFinnNAVKontorRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSFinnNAVKontorResponse;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentOverordnetEnhetListeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

@Slf4j
@Service
public class OrganisasjonEnhetService {

    private final OrganisasjonEnhetV2 organisasjonEnhetV2;

    @Autowired
    public OrganisasjonEnhetService(OrganisasjonEnhetV2 organisasjonEnhetV2) {
        this.organisasjonEnhetV2 = organisasjonEnhetV2;
    }

    public List<String> hentOverordnetEnhetForNAVKontor(String enhet) {
        try {
            return organisasjonEnhetV2.hentOverordnetEnhetListe(new WSHentOverordnetEnhetListeRequest()
                    .withEnhetId(enhet)
                    .withEnhetRelasjonstype(new WSEnhetRelasjonstyper().withValue("FYLKE")))
                    .getOverordnetEnhetListe()
                    .stream()
                    .map(WSOrganisasjonsenhet::getEnhetId)
                    .collect(toList());
        } catch (HentOverordnetEnhetListeEnhetIkkeFunnet | RuntimeException e) {
            log.info("Feil ved henting av NAV Ressurs sin fylkesenhet.", e);
            return emptyList();
        }
    }

    public List<String> finnNAVKontorForGT(String geografiskTilknytning) {
        try {
            return of(organisasjonEnhetV2.finnNAVKontor(
                    new WSFinnNAVKontorRequest()
                            .withGeografiskTilknytning(
                                    new WSGeografi()
                                            .withValue(geografiskTilknytning))))
                    .map(WSFinnNAVKontorResponse::getNAVKontor)
                    .filter(wsOrganisasjonsenhet -> WSEnhetsstatus.AKTIV.equals(wsOrganisasjonsenhet.getStatus()))
                    .map(WSOrganisasjonsenhet::getEnhetId)
                    .collect(toList());
        } catch (FinnNAVKontorUgyldigInput |
                RuntimeException e) {
            log.info("Finner ikke NAV-kontor for geografisk tilknytning " + geografiskTilknytning, e);
            return emptyList();
        }
    }

}
