package no.nav.syfo.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.config.CacheConfig.CACHENAME_VEILEDER_ENHETER;

@Slf4j
@Service
public class OrganisasjonRessursEnhetService {

    private final OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1;

    @Autowired
    public OrganisasjonRessursEnhetService(OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1) {
        this.organisasjonRessursEnhetV1 = organisasjonRessursEnhetV1;
    }

    @Cacheable(cacheNames = CACHENAME_VEILEDER_ENHETER, key = "#veilederId", condition = "#veilederId != null")
    public List<String> hentVeiledersEnheter(String veilederId) {
        try {
            return organisasjonRessursEnhetV1
                    .hentEnhetListe(new WSHentEnhetListeRequest().withRessursId(veilederId))
                    .getEnhetListe().stream()
                    .map(WSEnhet::getEnhetId)
                    .collect(toList());
        } catch (HentEnhetListeUgyldigInput | HentEnhetListeRessursIkkeFunnet e) {
            log.error("Feil ved henting av NAV Ressurs sin enhetliste.", e);
            return emptyList();
        }
    }

    public boolean harTilgangTilEnhet(String veilederId, String navEnhet) {
        return hentVeiledersEnheter(veilederId)
                .stream()
                .anyMatch(enhet -> enhet.equals(navEnhet));
    }
}
