package no.nav.syfo.services;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetRelasjonstyper;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentOverordnetEnhetListeRequest;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class OrganisasjonEnhetService {
    private static final Logger LOG = getLogger(OrganisasjonEnhetService.class);

    @Inject
    private OrganisasjonEnhetV2 organisasjonEnhetV2;

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
            LOG.info("Feil ved henting av NAV Ressurs sin fylkesenhet.", e);
            return emptyList();
        }
    }
}
