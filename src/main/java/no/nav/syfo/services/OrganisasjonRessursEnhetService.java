package no.nav.syfo.services;

import no.nav.brukerdialog.security.context.SubjectHandler;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.brukerdialog.security.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.*;

public class OrganisasjonRessursEnhetService {
    private static final Logger LOG = getLogger(OrganisasjonRessursEnhetService.class);

    @Inject
    private OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1;

    public List<String> hentVeiledersEnheter() {
        try {
            return organisasjonRessursEnhetV1
                    .hentEnhetListe(new WSHentEnhetListeRequest().withRessursId(getSubjectHandler().getUid()))
                    .getEnhetListe().stream()
                    .map(WSEnhet::getEnhetId)
                    .collect(toList());
        } catch (HentEnhetListeUgyldigInput | HentEnhetListeRessursIkkeFunnet e) {
            LOG.error("Feil ved henting av NAV Ressurs sin enhetliste.", e);
            return emptyList();
        }
    }
}
