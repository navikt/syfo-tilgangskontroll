package no.nav.syfo.api;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.services.LdapService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;

import static no.nav.syfo.domain.AdRoller.*;
import static no.nav.syfo.mocks.OrganisasjonRessursEnhetMock.*;
import static no.nav.syfo.mocks.PersonMock.*;
import static no.nav.syfo.util.LdapUtil.mockRoller;
import static no.nav.syfo.util.OidcTestHelper.loggInnVeilederMedAzure;
import static no.nav.syfo.util.OidcTestHelper.loggInnVeilederMedOpenAM;
import static org.junit.Assert.*;

/**
 * Komponent / blackbox test av møtebehovsfunskjonaliteten - test at endepunktet (controlleren, for enkelhets skyld)
 * gir riktig svar utifra hva web-servicene returnerer
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
public class TilgangRessursComponentTest {

    private static final boolean INNVILG = true;
    private static final boolean NEKT = false;

    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_FORBIDDEN = 403;

    @Autowired
    private OIDCRequestContextHolder oidcRequestContextHolder;

    @Autowired
    private LdapService ldapServiceMock; // TODO Forsøk å mocke selve ldap med f.eks spring.security.test

    @Autowired
    TilgangRessurs tilgangRessurs;

    @Test
    public void tilgangTilTjenestenInnvilget() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        assertEquals(HTTP_STATUS_OK, tilgangRessurs.tilgangTilTjenesten().getStatusCodeValue());
    }

    @Test
    public void tilgangTilTjenestenNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, NEKT, SYFO);

        assertEquals(HTTP_STATUS_FORBIDDEN, tilgangRessurs.tilgangTilTjenesten().getStatusCodeValue());
    }

    @Test
    public void tilgangTilBrukerInnvilget() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBruker(BJARNE_BRUKER);
        assertTilgangOK(response);
    }

    //TODO test geografi

    @Test
    public void tilgangTilKode6BrukerNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBruker(BENGT_KODE6_BRUKER);
        assertTilgangNektet(response, KODE6.name());
    }

    @Test
    public void tilgangTilKode6BrukerNektesAlltid() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO, KODE6);

        ResponseEntity response = tilgangRessurs.tilgangTilBruker(BENGT_KODE6_BRUKER);
        assertTilgangNektet(response, KODE6.name());
    }

    @Test
    public void tilgangTilKode7BrukerNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBruker(BIRTE_KODE7_BRUKER);
        assertTilgangNektet(response, KODE7.name());
    }

    @Test
    public void tilgangTilKode7BrukerInnvilget() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO, KODE7);

        ResponseEntity response = tilgangRessurs.tilgangTilBruker(BIRTE_KODE7_BRUKER);
        assertTilgangOK(response);
    }

    @Test
    public void tilgangTilEgenAnsattBrukerNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBruker(ERIK_EGENANSATT_BRUKER);
        assertTilgangNektet(response, EGEN_ANSATT.name());
    }

    @Test
    public void tilgangTilEgenAnsattBrukerInnvilget() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO, EGEN_ANSATT);

        ResponseEntity response = tilgangRessurs.tilgangTilBruker(ERIK_EGENANSATT_BRUKER);
        assertTilgangOK(response);
    }

    @Test
    public void tilgangTilEnhetInnvilget() throws ParseException{
        loggInnVeilederMedAzure(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        assertEquals(HTTP_STATUS_OK, tilgangRessurs.tilgangTilEnhet(ENHET_1_ID).getStatusCodeValue());
    }

    @Test
    public void tilgangTilEnhetNektet() throws ParseException{
        loggInnVeilederMedAzure(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        assertEquals(HTTP_STATUS_FORBIDDEN, tilgangRessurs.tilgangTilEnhet(ENHET_3_ID).getStatusCodeValue());
    }

    private void assertTilgangOK(ResponseEntity response) {
        assertEquals(HTTP_STATUS_OK, response.getStatusCodeValue());
        Tilgang tilgang = (Tilgang) response.getBody();
        assertTrue(tilgang.isHarTilgang());
    }

    private void assertTilgangNektet(ResponseEntity response, String begrunnelse) {
        assertEquals(HTTP_STATUS_FORBIDDEN, response.getStatusCodeValue());
        Tilgang tilgang = (Tilgang) response.getBody();
        assertFalse(tilgang.isHarTilgang());
        assertEquals(begrunnelse, tilgang.getBegrunnelse());
    }

}