package no.nav.syfo.services;

import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.domain.PersonInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.syfo.domain.AdRoller.REGIONAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeografiskTilgangServiceTest {

    private static final String VEILEDER_UID = "Z999999";
    private static final PersonInfo PERSON_INFO = new PersonInfo("1", "brukersPostnummer");
    private static final String BRUKERS_ENHET = "brukersEnhet";
    private static final String VEILEDERS_ENHET = "veiledersEnhet";
    private static final String OVERORDNET_ENHET = "fylkeskontor";

    @Mock
    private LdapService ldapService;
    @Mock
    private OrganisasjonRessursEnhetService organisasjonRessursEnhetService;
    @Mock
    private OrganisasjonEnhetService organisasjonEnhetService;
    @InjectMocks
    private GeografiskTilgangService geografiskTilgangService;

    @Before
    public void setup() {
        when(ldapService.harTilgang(anyString(), any())).thenReturn(false);
        when(organisasjonEnhetService.finnNAVKontorForGT(PERSON_INFO.geografiskTilknytning())).thenReturn(asList(BRUKERS_ENHET, "enAnnenEnhet"));
    }

    @Test
    public void nasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang(VEILEDER_UID, AdRoller.NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_INFO)).isTrue();
    }

    @Test
    public void utvidetTilNasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang(VEILEDER_UID, AdRoller.UTVIDBAR_TIL_NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_INFO)).isTrue();
    }

    @Test
    public void harTilgangHvisVeilederHarTilgangTilSammeEnhetSomBruker() {
        when(organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(asList(BRUKERS_ENHET, "enHeltAnnenEnhet"));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_INFO)).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisVeilederIkkeHarTilgangTilSammeEnhetSomBruker() {
        when( organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(singletonList("enHeltAnnenEnhet"));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_INFO)).isFalse();
    }

    @Test
    public void harTilgangHvisRegionalTilgangOgTilgangTilEnhetensFylkeskontor() {
        when(ldapService.harTilgang(VEILEDER_UID, REGIONAL.rolle)).thenReturn(true);
        when(organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(singletonList(VEILEDERS_ENHET));
        when(organisasjonEnhetService.hentOverordnetEnhetForNAVKontor(VEILEDERS_ENHET)).thenReturn(singletonList(OVERORDNET_ENHET));
        when(organisasjonEnhetService.hentOverordnetEnhetForNAVKontor(BRUKERS_ENHET)).thenReturn(singletonList(OVERORDNET_ENHET));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_INFO)).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisTilgangTilEnhetensFylkeskontorMenIkkeRegionalTilgang() {
        when(organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(singletonList(OVERORDNET_ENHET));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_INFO)).isFalse();
    }
}
