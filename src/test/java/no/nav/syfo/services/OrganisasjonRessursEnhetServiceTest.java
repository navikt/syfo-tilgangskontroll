package no.nav.syfo.services;

import no.nav.brukerdialog.security.context.CustomizableSubjectHandler;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.brukerdialog.security.context.CustomizableSubjectHandler.setUid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrganisasjonRessursEnhetServiceTest {

    @Mock
    private OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1;

    @InjectMocks
    private OrganisasjonRessursEnhetService organisasjonRessursEnhetService;

    private static final String BRUKER = "Z999999";
    private static final String BRUKERS_ENHET_1 = "brukersEnhet1";
    private static final String BRUKERS_ENHET_2 = "brukersEnhet2";
    private static final String EN_ANNEN_ENHET = "enAnnenEnhet";

    @Before
    public void setup() throws HentEnhetListeUgyldigInput, HentEnhetListeRessursIkkeFunnet {
        setProperty("no.nav.brukerdialog.security.context.subjectHandlerImplementationClass", CustomizableSubjectHandler.class.getName());
        setUid(BRUKER);
        WSHentEnhetListeResponse respons = new WSHentEnhetListeResponse().withEnhetListe(asList(
                new WSEnhet().withEnhetId(BRUKERS_ENHET_1),
                new WSEnhet().withEnhetId(BRUKERS_ENHET_2)
        ));
        when(organisasjonRessursEnhetV1.hentEnhetListe(any(WSHentEnhetListeRequest.class))).thenReturn(respons);
    }

    @Test
    public void harTilgangTilEnhetSkalReturnereTrue() {
        assertThat(organisasjonRessursEnhetService.harTilgangTilEnhet(BRUKERS_ENHET_1)).isEqualTo(true);
        assertThat(organisasjonRessursEnhetService.harTilgangTilEnhet(BRUKERS_ENHET_2)).isEqualTo(true);
    }

    @Test
    public void harTilgangTilEnhetSkalReturnereFalse() {
        assertThat(organisasjonRessursEnhetService.harTilgangTilEnhet(EN_ANNEN_ENHET)).isEqualTo(false);
    }

}
