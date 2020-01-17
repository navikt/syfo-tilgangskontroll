package no.nav.syfo.api;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.services.LdapService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;

import static no.nav.syfo.domain.AdRoller.*;
import static no.nav.syfo.mocks.OrganisasjonRessursEnhetMock.VIGGO_VEILEDER;
import static no.nav.syfo.mocks.PersonMock.*;
import static no.nav.syfo.util.LdapUtil.mockRoller;
import static no.nav.syfo.util.OidcTestHelper.logInVeilederWithAzure2;
import static no.nav.syfo.util.OidcTestHelper.loggUtAlle;
import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
public class AccessToRessursViaAzure2ComponentTest {

    private static final boolean INNVILG = true;
    private static final boolean NEKT = false;

    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_FORBIDDEN = 403;

    @Autowired
    private OIDCRequestContextHolder oidcRequestContextHolder;

    @Autowired
    private LdapService ldapServiceMock;

    @Autowired
    TilgangRessurs tilgangRessurs;

    @Before
    public void setup() throws ParseException {
        logInVeilederWithAzure2(oidcRequestContextHolder, VIGGO_VEILEDER);
    }

    @After
    public void tearDown() {
        loggUtAlle(oidcRequestContextHolder);
    }

    @Test
    public void accessToBrukerGranted() {
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BJARNE_BRUKER);
        assertAccessOk(response);
    }

    @Test
    public void accessToKode6PersonDenied() {
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BENGT_KODE6_BRUKER);
        assertAccessDenied(response, KODE6.name());
    }

    @Test
    public void accessToKode6PersonAlwaysDenied() {
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO, KODE6);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BENGT_KODE6_BRUKER);
        assertAccessDenied(response, KODE6.name());
    }

    @Test
    public void accessToKode7PersonDenied() {
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BIRTE_KODE7_BRUKER);
        assertAccessDenied(response, KODE7.name());
    }

    @Test
    public void accessToKode7PersonGranted() {
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO, KODE7);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(BIRTE_KODE7_BRUKER);
        assertAccessOk(response);
    }

    @Test
    public void accessToEgenAnsattPersonDenied() {
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO);

        ResponseEntity response = tilgangRessurs.accessToPersonViaAzure(ERIK_EGENANSATT_BRUKER);
        assertAccessDenied(response, EGEN_ANSATT.name());
    }

    @Test
    public void accessToEgenAnsattPersonGranted() {
        mockRoller(ldapServiceMock, VIGGO_VEILEDER, INNVILG, SYFO, EGEN_ANSATT);

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
