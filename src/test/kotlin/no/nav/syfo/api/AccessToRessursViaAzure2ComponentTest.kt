package no.nav.syfo.api

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.axsys.AxsysEnhet
import no.nav.syfo.consumer.ldap.LdapService
import no.nav.syfo.consumer.msgraph.TokenConsumer
import no.nav.syfo.consumer.norg2.NorgConsumer
import no.nav.syfo.consumer.pdl.*
import no.nav.syfo.consumer.skjermedepersoner.SkjermedePersonerPipConsumer
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.testhelper.LdapUtil.mockRoller
import no.nav.syfo.testhelper.OidcTestHelper.logInVeilederWithAzure2
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.BENGT_KODE6_BRUKER
import no.nav.syfo.testhelper.UserConstants.BIRTE_KODE7_BRUKER
import no.nav.syfo.testhelper.UserConstants.BJARNE_BRUKER
import no.nav.syfo.testhelper.UserConstants.ERIK_EGENANSATT_BRUKER
import no.nav.syfo.testhelper.UserConstants.NAV_ENHETID_1
import no.nav.syfo.testhelper.UserConstants.NAV_ENHETID_2
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.tilgang.Tilgang
import no.nav.syfo.tilgang.TilgangController
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.text.ParseException

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LocalApplication::class])
class AccessToRessursViaAzure2ComponentTest {
    @MockBean
    private lateinit var axsysConsumer: AxsysConsumer

    @MockBean
    private lateinit var norgConsumer: NorgConsumer

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @MockBean
    private lateinit var skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer

    @MockBean
    private lateinit var tokenConsumer: TokenConsumer

    @Autowired
    private lateinit var oidcRequestContextHolder: TokenValidationContextHolder

    @Autowired
    private lateinit var ldapServiceMock: LdapService

    @Value("\${smregistrering.client.id}")
    private lateinit var smregistreringClientId: String

    @Autowired
    lateinit var tilgangController: TilgangController

    @BeforeEach
    @Throws(ParseException::class)
    fun setup() {
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(
            listOf(
                AxsysEnhet(
                    NAV_ENHETID_1,
                    NAV_ENHET_NAVN
                ),
                AxsysEnhet(
                    NAV_ENHETID_2,
                    NAV_ENHET_NAVN
                ))
        )
        Mockito.`when`(norgConsumer.getNAVKontorForGT(
            GeografiskTilknytning(
                type = GeografiskTilknytningType.BYDEL,
                value = NAV_ENHETID_1,
            )
        )).thenReturn(
            NAV_ENHETID_1
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(ArgumentMatchers.anyString())).thenReturn(GeografiskTilknytning(
            type = GeografiskTilknytningType.BYDEL,
            value = "0330",
        ))
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BJARNE_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BENGT_KODE6_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BIRTE_KODE7_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(ERIK_EGENANSATT_BRUKER)).thenReturn(true)
        Mockito.`when`(tokenConsumer.getSubjectFromMsGraph(ArgumentMatchers.anyString())).thenReturn(
            VEILEDER_ID
        )
        logInVeilederWithAzure2(oidcRequestContextHolder, "", VEILEDER_ID)
    }

    @AfterEach
    fun tearDown() {
        loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    fun accessToSYFOGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        val response = tilgangController.accessToSYFOViaAzure()
        assertAccessOk(response)
    }

    @Test
    fun accessToSYFODenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, NEKT, AdRoller.SYFO)
        val response = tilgangController.accessToSYFOViaAzure()
        assertAccessDenied(response, AdRoller.SYFO.name)
    }

    @Test
    fun accessToBrukerGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        val response = tilgangController.accessToPersonViaAzure(BJARNE_BRUKER)
        assertAccessOk(response)
    }

    @Test
    fun accessToKode6PersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangController.accessToPersonViaAzure(BENGT_KODE6_BRUKER)
        assertAccessDenied(response, AdRoller.KODE6.name)
    }

    @Test
    fun `access to Kode6-person always denied by default`() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.KODE6)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangController.accessToPersonViaAzure(BENGT_KODE6_BRUKER)
        assertAccessDenied(response, AdRoller.KODE6.name)
    }

    @Test
    fun `access to Kode6-person granted for smregistrering and NAVIdent with Kode6 access`() {
        loggUtAlle(oidcRequestContextHolder)
        logInVeilederWithAzure2(oidcRequestContextHolder, smregistreringClientId, VEILEDER_ID)

        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.KODE6)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangController.accessToPersonViaAzure(BENGT_KODE6_BRUKER)
        assertAccessOk(response)
    }

    @Test
    fun accessToKode7PersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode7(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangController.accessToPersonViaAzure(BIRTE_KODE7_BRUKER)
        assertAccessDenied(response, AdRoller.KODE7.name)
    }

    @Test
    fun accessToKode7PersonGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.KODE7)
        val response = tilgangController.accessToPersonViaAzure(BIRTE_KODE7_BRUKER)
        assertAccessOk(response)
    }

    @Test
    fun accessToEgenAnsattPersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        val response = tilgangController.accessToPersonViaAzure(ERIK_EGENANSATT_BRUKER)
        assertAccessDenied(response, AdRoller.EGEN_ANSATT.name)
    }

    @Test
    fun accessToEgenAnsattPersonGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.EGEN_ANSATT)
        val response = tilgangController.accessToPersonViaAzure(ERIK_EGENANSATT_BRUKER)
        assertAccessOk(response)
    }

    private fun assertAccessOk(response: ResponseEntity<*>) {
        assertEquals(HTTP_STATUS_OK.toLong(), response.statusCodeValue.toLong())
        val tilgang = response.body as Tilgang
        assertTrue(tilgang.harTilgang)
    }

    private fun assertAccessDenied(response: ResponseEntity<*>, begrunnelse: String) {
        assertEquals(HTTP_STATUS_FORBIDDEN.toLong(), response.statusCodeValue.toLong())
        val tilgang = response.body as Tilgang
        assertFalse(tilgang.harTilgang)
        assertEquals(begrunnelse, tilgang.begrunnelse)
    }

    companion object {
        private const val INNVILG = true
        private const val NEKT = false
        private const val HTTP_STATUS_OK = 200
        private const val HTTP_STATUS_FORBIDDEN = 403
    }
}
