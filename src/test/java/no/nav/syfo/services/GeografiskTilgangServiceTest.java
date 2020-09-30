package no.nav.syfo.services;

import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.axsys.AxsysEnhet;
import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.norg2.NorgConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.syfo.domain.AdRoller.REGIONAL;
import static no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeografiskTilgangServiceTest {

    private static final String VEILEDER_UID = "Z999999";
    private static final String BRUKERS_ENHET = "brukersEnhet";
    private static final String VEILEDERS_ENHET = "veiledersEnhet";
    private static final String OVERORDNET_ENHET = "fylkeskontor";
    private static final String GEOGRAFISK_TILKNYTNING = "brukersPostnummer";

    @Mock
    private AxsysConsumer axsysConsumer;
    @Mock
    private LdapService ldapService;
    @Mock
    private NorgConsumer norgConsumer;
    @InjectMocks
    private GeografiskTilgangService geografiskTilgangService;

    @Before
    public void setup() {
        when(ldapService.harTilgang(anyString(), any())).thenReturn(false);
        when(norgConsumer.getNAVKontorForGT(GEOGRAFISK_TILKNYTNING)).thenReturn(
                BRUKERS_ENHET
        );
    }

    @Test
    public void nasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang(VEILEDER_UID, AdRoller.NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, GEOGRAFISK_TILKNYTNING)).isTrue();
    }

    @Test
    public void utvidetTilNasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang(VEILEDER_UID, AdRoller.UTVIDBAR_TIL_NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, GEOGRAFISK_TILKNYTNING)).isTrue();
    }

    @Test
    public void harTilgangHvisVeilederHarTilgangTilSammeEnhetSomBruker() {
        when(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
                asList(
                        new AxsysEnhet(
                                BRUKERS_ENHET,
                                NAV_ENHET_NAVN
                        ),
                        new AxsysEnhet(
                                "enHeltAnnenEnhet",
                                NAV_ENHET_NAVN
                        ))
        );
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, GEOGRAFISK_TILKNYTNING)).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisVeilederIkkeHarTilgangTilSammeEnhetSomBruker() {
        when(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
                singletonList(new AxsysEnhet(
                        "enHeltAnnenEnhet",
                        NAV_ENHET_NAVN
                ))
        );
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, GEOGRAFISK_TILKNYTNING)).isFalse();
    }

    @Test
    public void harTilgangHvisRegionalTilgangOgTilgangTilEnhetensFylkeskontor() {
        when(ldapService.harTilgang(VEILEDER_UID, REGIONAL.rolle)).thenReturn(true);
        when(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
                singletonList(new AxsysEnhet(
                        VEILEDERS_ENHET,
                        NAV_ENHET_NAVN
                ))
        );
        when(norgConsumer.getOverordnetEnhetListForNAVKontor(VEILEDERS_ENHET)).thenReturn(singletonList(OVERORDNET_ENHET));
        when(norgConsumer.getOverordnetEnhetListForNAVKontor(BRUKERS_ENHET)).thenReturn(singletonList(OVERORDNET_ENHET));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, GEOGRAFISK_TILKNYTNING)).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisTilgangTilEnhetensFylkeskontorMenIkkeRegionalTilgang() {
        when(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
                singletonList(new AxsysEnhet(
                        OVERORDNET_ENHET,
                        NAV_ENHET_NAVN
                ))
        );
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, GEOGRAFISK_TILKNYTNING)).isFalse();
    }
}
