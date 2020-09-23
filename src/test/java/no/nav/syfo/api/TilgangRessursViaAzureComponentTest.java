package no.nav.syfo.api;

import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.axsys.AxsysEnhet;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.norg2.NorgConsumer;
import no.nav.syfo.services.LdapService;
import no.nav.syfo.skjermedepersoner.SkjermedePersonerPipConsumer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.syfo.domain.AdRoller.*;
import static no.nav.syfo.mocks.PersonMock.*;
import static no.nav.syfo.testhelper.Norg2MockKt.generateNorgEnhet;
import static no.nav.syfo.testhelper.UserConstants.*;
import static no.nav.syfo.util.LdapUtil.mockRoller;
import static no.nav.syfo.util.OidcTestHelper.loggInnVeilederMedAzure;
import static no.nav.syfo.util.OidcTestHelper.loggUtAlle;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Komponent / blackbox test av møtebehovsfunskjonaliteten (med Azure innlogging) - test at endepunktet (controlleren, for enkelhets skyld)
 * gir riktig svar utifra hva web-servicene returnerer
 */

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
public class TilgangRessursViaAzureComponentTest {

    private static final boolean INNVILG = true;
    private static final boolean NEKT = false;

    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_FORBIDDEN = 403;

    @MockBean
    private AxsysConsumer axsysConsumer;
    @MockBean
    private NorgConsumer norgConsumer;
    @MockBean
    private SkjermedePersonerPipConsumer skjermedePersonerPipConsumer;
    @Autowired
    private TokenValidationContextHolder oidcRequestContextHolder;

    @Autowired
    private LdapService ldapServiceMock; // TODO Forsøk å mocke selve ldap med f.eks spring.security.test

    @Autowired
    TilgangRessurs tilgangRessurs;

    @Before
    public void setup() throws ParseException {
        when(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(
                asList(
                        new AxsysEnhet(
                                NAV_ENHETID_1,
                                NAV_ENHET_NAVN
                        ),
                        new AxsysEnhet(
                                NAV_ENHETID_2,
                                NAV_ENHET_NAVN
                        ))
        );
        when(norgConsumer.getNAVKontorForGT(__0330.getGeografiskTilknytning())).thenReturn(
                generateNorgEnhet(NAV_ENHETID_1)
        );
        when(skjermedePersonerPipConsumer.erSkjermet(BJARNE_BRUKER)).thenReturn(false);
        when(skjermedePersonerPipConsumer.erSkjermet(BENGT_KODE6_BRUKER)).thenReturn(false);
        when(skjermedePersonerPipConsumer.erSkjermet(BIRTE_KODE7_BRUKER)).thenReturn(false);
        when(skjermedePersonerPipConsumer.erSkjermet(ERIK_EGENANSATT_BRUKER)).thenReturn(true);
        loggInnVeilederMedAzure(oidcRequestContextHolder, VEILEDER_ID);
    }

    @After
    public void tearDown() {
        loggUtAlle(oidcRequestContextHolder);
    }

    @Test
    public void tilgangTilTjenestenInnvilget() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        assertEquals(HTTP_STATUS_OK, tilgangRessurs.tilgangTilTjenestenViaAzure().getStatusCodeValue());
    }

    @Test
    public void tilgangTilTjenestenNektet() {
        mockRoller(ldapServiceMock, VEILEDER_ID, NEKT, SYFO);

        assertEquals(HTTP_STATUS_FORBIDDEN, tilgangRessurs.tilgangTilTjenestenViaAzure().getStatusCodeValue());
    }

    @Test
    public void tilgangTilBrukerInnvilget() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukerViaAzure(BJARNE_BRUKER);
        assertTilgangOK(response);
    }

    @Test
    public void tilgangTilKode6BrukerNektet() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukerViaAzure(BENGT_KODE6_BRUKER);
        assertTilgangNektet(response, KODE6.name());
    }

    @Test
    public void tilgangTilKode6BrukerNektesAlltid() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO, KODE6);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukerViaAzure(BENGT_KODE6_BRUKER);
        assertTilgangNektet(response, KODE6.name());
    }

    @Test
    public void tilgangTilKode7BrukerNektet() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukerViaAzure(BIRTE_KODE7_BRUKER);
        assertTilgangNektet(response, KODE7.name());
    }

    @Test
    public void tilgangTilKode7BrukerInnvilget() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO, KODE7);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukerViaAzure(BIRTE_KODE7_BRUKER);
        assertTilgangOK(response);
    }

    @Test
    public void tilgangTilBrukereSYFO() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukereViaAzure(asList(
                BJARNE_BRUKER,
                BENGT_KODE6_BRUKER,
                BIRTE_KODE7_BRUKER,
                ERIK_EGENANSATT_BRUKER
        ));
        assertEquals(response.getStatusCodeValue(), 200);
        assertEquals(response.getBody(), singletonList(BJARNE_BRUKER));
    }

    @Test
    public void tilgangTilEgenAnsattBrukerNektet() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukerViaAzure(ERIK_EGENANSATT_BRUKER);
        assertTilgangNektet(response, EGEN_ANSATT.name());
    }

    @Test
    public void tilgangTilEgenAnsattBrukerInnvilget() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO, EGEN_ANSATT);

        ResponseEntity response = tilgangRessurs.tilgangTilBrukerViaAzure(ERIK_EGENANSATT_BRUKER);
        assertTilgangOK(response);
    }

    @Test
    public void tilgangTilEnhetInnvilget() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);
        when(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(singletonList(
                new AxsysEnhet(
                        NAV_ENHETID_1,
                        NAV_ENHET_NAVN
                )
        ));

        assertEquals(HTTP_STATUS_OK, tilgangRessurs.tilgangTilEnhet(NAV_ENHETID_1).getStatusCodeValue());
    }

    @Test
    public void tilgangTilEnhetNektet() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        assertEquals(HTTP_STATUS_FORBIDDEN, tilgangRessurs.tilgangTilEnhet(NAV_ENHETID_3).getStatusCodeValue());
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
