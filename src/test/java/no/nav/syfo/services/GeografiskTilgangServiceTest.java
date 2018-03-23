package no.nav.syfo.services;

import no.nav.brukerdialog.security.context.CustomizableSubjectHandler;
import no.nav.brukerdialog.security.context.ThreadLocalSubjectHandler;
import no.nav.syfo.domain.AdRoller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.brukerdialog.security.context.CustomizableSubjectHandler.*;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeografiskTilgangServiceTest {

    @Mock
    private LdapService ldapService;
    @Mock
    private PersonService personService;
    @Mock
    private OrganisasjonRessursEnhetService organisasjonRessursEnhetService;
    @Mock
    private OrganisasjonEnhetService organisasjonEnhetService;
    @InjectMocks
    private GeografiskTilgangService geografiskTilgangService;

    @Before
    public void setup() {
        setProperty("no.nav.brukerdialog.security.context.subjectHandlerImplementationClass", CustomizableSubjectHandler.class.getName());
        setUid("Z999999");

        when(ldapService.harTilgang(anyString(), any())).thenReturn(false);
        when(personService.hentGeografiskTilknytning(anyString())).thenReturn("brukersEnhet");
    }

    @Test
    public void nasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang("Z999999", AdRoller.NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang("fnr")).isTrue();
    }

    @Test
    public void utvidetTilNasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang("Z999999", AdRoller.UTVIDBAR_TIL_NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang("fnr")).isTrue();
    }

    @Test
    public void harTilgangHvisVeilederHarTilgangTilSammeEnhetSomBruker() {
        when( organisasjonRessursEnhetService.hentVeiledersEnheter()).thenReturn(asList("brukersEnhet", "enannenenhet"));
        assertThat(geografiskTilgangService.harGeografiskTilgang("fnr")).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisVeilederIkkeHarTilgangTilSammeEnhetSomBruker() {
        when( organisasjonRessursEnhetService.hentVeiledersEnheter()).thenReturn(asList("enannenenhet"));
        assertThat(geografiskTilgangService.harGeografiskTilgang("fnr")).isFalse();
    }

    @Test
    public void harTilgangHvisRegionalTilgangOgTilgangTilEnhetensFylkeskontor() {
        when(ldapService.harTilgang("Z999999", AdRoller.REGIONAL.rolle)).thenReturn(true);
        when(organisasjonRessursEnhetService.hentVeiledersEnheter()).thenReturn(asList("fylkeskontor"));
        when(organisasjonEnhetService.hentOverordnetEnhetForNAVKontor("brukersEnhet")).thenReturn(asList("fylkeskontor"));
        assertThat(geografiskTilgangService.harGeografiskTilgang("fnr")).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisTilgangTilEnhetensFylkeskontorMenIkkeRegionalTilgang() {
        when(organisasjonRessursEnhetService.hentVeiledersEnheter()).thenReturn(asList("fylkeskontor"));
        when(organisasjonEnhetService.hentOverordnetEnhetForNAVKontor("brukersEnhet")).thenReturn(asList("fylkeskontor"));
        assertThat(geografiskTilgangService.harGeografiskTilgang("fnr")).isFalse();
    }
}
