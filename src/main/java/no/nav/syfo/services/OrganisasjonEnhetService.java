package no.nav.syfo.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetRelasjonstyper;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentOverordnetEnhetListeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.config.CacheConfig.CACHENAME_ENHET_OVERORDNET_ENHETER;

@Slf4j
@Service
public class OrganisasjonEnhetService {

    private final OrganisasjonEnhetV2 organisasjonEnhetV2;

    @Autowired
    public OrganisasjonEnhetService(OrganisasjonEnhetV2 organisasjonEnhetV2) {
        this.organisasjonEnhetV2 = organisasjonEnhetV2;
    }

    @Cacheable(cacheNames = CACHENAME_ENHET_OVERORDNET_ENHETER, key = "#enhet", condition = "#enhet != null")
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
}
