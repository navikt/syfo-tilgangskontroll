package no.nav.syfo.services;

import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class OrganisasjonRessursEnhetService {
    private static final Logger LOG = getLogger(OrganisasjonRessursEnhetService.class);

    @Autowired
    private OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1;

    public List<String> hentVeiledersEnheter(String veilederId) {
        try {
            return organisasjonRessursEnhetV1
                    .hentEnhetListe(new WSHentEnhetListeRequest().withRessursId(veilederId))
                    .getEnhetListe().stream()
                    .map(WSEnhet::getEnhetId)
                    .collect(toList());
        } catch (HentEnhetListeUgyldigInput | HentEnhetListeRessursIkkeFunnet e) {
            LOG.error("Feil ved henting av NAV Ressurs sin enhetliste.", e);
            return emptyList();
        }
    }

    public boolean harTilgangTilEnhet(String navEnhet) {
        return hentVeiledersEnheter()
                .stream()
                .anyMatch(enhet -> enhet.equals(navEnhet));
    }
}
