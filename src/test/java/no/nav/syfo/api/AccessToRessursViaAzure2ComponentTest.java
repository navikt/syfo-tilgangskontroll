package no.nav.syfo.api;

import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.axsys.AxsysConsumer;
import no.nav.syfo.axsys.AxsysEnhet;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.norg2.NorgConsumer;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.security.TokenConsumer;
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
import static no.nav.syfo.domain.AdRoller.*;
import static no.nav.syfo.testhelper.UserConstants.*;
import static no.nav.syfo.util.LdapUtil.mockRoller;
import static no.nav.syfo.testhelper.OidcTestHelper.logInVeilederWithAzure2;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
public class AccessToRessursViaAzure2ComponentTest {

    private static final boolean INNVILG = true;
    private static final boolean NEKT = false;

    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_FORBIDDEN = 403;

    @MockBean
    private AxsysConsumer axsysConsumer;

    @MockBean
    private NorgConsumer norgConsumer;

    @MockBean
    private PdlConsumer pdlConsumer;

    @MockBean
    private SkjermedePersonerPipConsumer skjermedePersonerPipConsumer;

    @MockBean
    private TokenConsumer tokenConsumer;

    @Autowired
    private TokenValidationContextHolder oidcRequestContextHolder;

    @Autowired
    private LdapService ldapServiceMock;

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
        when(norgConsumer.getNAVKontorForGT(NAV_ENHETID_1)).thenReturn(
                NAV_ENHETID_1
        );
        when(pdlConsumer.geografiskTilknytning(anyString())).thenReturn("0330");
        when(skjermedePersonerPipConsumer.erSkjermet(BJARNE_BRUKER)).thenReturn(false);
        when(skjermedePersonerPipConsumer.erSkjermet(BENGT_KODE6_BRUKER)).thenReturn(false);
        when(skjermedePersonerPipConsumer.erSkjermet(BIRTE_KODE7_BRUKER)).thenReturn(false);
        when(skjermedePersonerPipConsumer.erSkjermet(ERIK_EGENANSATT_BRUKER)).thenReturn(true);
        when(tokenConsumer.getSubjectFromMsGraph(any(TokenValidationContextHolder.class))).thenReturn(
                VEILEDER_ID
        );
        logInVeilederWithAzure2(oidcRequestContextHolder, VEILEDER_ID);
    }

    @After
    public void tearDown() {
        loggUtAlle(oidcRequestContextHolder);
    }

    @Test
    public void accessToBrukerGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BJARNE_BRUKER);
        assertAccessOk(response);
    }

    @Test
    public void accessToKode6PersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);
        when(pdlConsumer.isKode6(any())).thenReturn(true);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BENGT_KODE6_BRUKER);
        assertAccessDenied(response, KODE6.name());
    }

    @Test
    public void accessToKode6PersonAlwaysDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO, KODE6);
        when(pdlConsumer.isKode6(any())).thenReturn(true);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BENGT_KODE6_BRUKER);
        assertAccessDenied(response, KODE6.name());
    }

    @Test
    public void accessToKode7PersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);
        when(pdlConsumer.isKode7(any())).thenReturn(true);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BIRTE_KODE7_BRUKER);
        assertAccessDenied(response, KODE7.name());
    }

    @Test
    public void accessToKode7PersonGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO, KODE7);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BIRTE_KODE7_BRUKER);
        assertAccessOk(response);
    }

    @Test
    public void accessToEgenAnsattPersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(ERIK_EGENANSATT_BRUKER);
        assertAccessDenied(response, EGEN_ANSATT.name());
    }

    @Test
    public void accessToEgenAnsattPersonGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, SYFO, EGEN_ANSATT);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(ERIK_EGENANSATT_BRUKER);
        assertAccessOk(response);
    }

    private void assertAccessOk(ResponseEntity response) {
        assertEquals(HTTP_STATUS_OK, response.getStatusCodeValue());
        Tilgang tilgang = (Tilgang) response.getBody();
        assertTrue(tilgang.isHarTilgang());
    }

    private void assertAccessDenied(ResponseEntity response, String begrunnelse) {
        assertEquals(HTTP_STATUS_FORBIDDEN, response.getStatusCodeValue());
        Tilgang tilgang = (Tilgang) response.getBody();
        assertFalse(tilgang.isHarTilgang());
        assertEquals(begrunnelse, tilgang.getBegrunnelse());
    }

}
