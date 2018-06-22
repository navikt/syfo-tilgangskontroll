package no.nav.syfo.services;

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EgenAnsattServiceTest {

    @Mock
    private EgenAnsattV1 egenAnsattV1;

    @InjectMocks
    private EgenAnsattService egenAnsattService;

    @Test
    public void egenAnsattGirTrue() {
        WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse response = new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse();
        response.setEgenAnsatt(true);
        when(egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(any(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest.class))).thenReturn(response);
        assertThat(egenAnsattService.erEgenAnsatt("fnr")).isTrue();
    }

    @Test
    public void ikkeEgenAnsattGirFalse() {
        WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse response = new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse();
        response.setEgenAnsatt(false);
        when(egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(any(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest.class))).thenReturn(response);
        assertThat(egenAnsattService.erEgenAnsatt("fnr")).isFalse();
    }

    @Test(expected = RuntimeException.class)
    public void ikkeTilgangTilTjenesteGirRuntimeException(){
        when(egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(any())).thenThrow(new RuntimeException("Noe gikk galt"));
        egenAnsattService.erEgenAnsatt("fnr");
    }
}