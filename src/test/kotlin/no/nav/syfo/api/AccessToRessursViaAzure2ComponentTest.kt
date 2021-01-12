package no.nav.syfo.api

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.axsys.AxsysConsumer
import no.nav.syfo.axsys.AxsysEnhet
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.domain.Tilgang
import no.nav.syfo.norg2.NorgConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.security.TokenConsumer
import no.nav.syfo.services.LdapService
import no.nav.syfo.skjermedepersoner.SkjermedePersonerPipConsumer
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
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.text.ParseException

@ActiveProfiles("test")
@RunWith(SpringRunner::class)
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

    @Autowired
    lateinit var tilgangRessurs: TilgangRessurs

    @Before
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
        Mockito.`when`(norgConsumer.getNAVKontorForGT(NAV_ENHETID_1)).thenReturn(
            NAV_ENHETID_1
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(ArgumentMatchers.anyString())).thenReturn("0330")
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BJARNE_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BENGT_KODE6_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BIRTE_KODE7_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(ERIK_EGENANSATT_BRUKER)).thenReturn(true)
        Mockito.`when`(tokenConsumer.getSubjectFromMsGraph(ArgumentMatchers.anyString())).thenReturn(
            VEILEDER_ID
        )
        logInVeilederWithAzure2(oidcRequestContextHolder, VEILEDER_ID)
    }

    @After
    fun tearDown() {
        loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    fun accessToBrukerGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        val response = tilgangRessurs.accessToPersonViaAzure(BJARNE_BRUKER)
        assertAccessOk(response)
    }

    @Test
    fun accessToKode6PersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangRessurs.accessToPersonViaAzure(BENGT_KODE6_BRUKER)
        assertAccessDenied(response, AdRoller.KODE6.name)
    }

    @Test
    fun accessToKode6PersonAlwaysDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.KODE6)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangRessurs.accessToPersonViaAzure(BENGT_KODE6_BRUKER)
        assertAccessDenied(response, AdRoller.KODE6.name)
    }

    @Test
    fun accessToKode7PersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode7(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangRessurs.accessToPersonViaAzure(BIRTE_KODE7_BRUKER)
        assertAccessDenied(response, AdRoller.KODE7.name)
    }

    @Test
    fun accessToKode7PersonGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.KODE7)
        val response = tilgangRessurs.accessToPersonViaAzure(BIRTE_KODE7_BRUKER)
        assertAccessOk(response)
    }

    @Test
    fun accessToEgenAnsattPersonDenied() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        val response = tilgangRessurs.accessToPersonViaAzure(ERIK_EGENANSATT_BRUKER)
        assertAccessDenied(response, AdRoller.EGEN_ANSATT.name)
    }

    @Test
    fun accessToEgenAnsattPersonGranted() {
        mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.EGEN_ANSATT)
        val response = tilgangRessurs.accessToPersonViaAzure(ERIK_EGENANSATT_BRUKER)
        assertAccessOk(response)
    }

    private fun assertAccessOk(response: ResponseEntity<*>) {
        Assert.assertEquals(HTTP_STATUS_OK.toLong(), response.statusCodeValue.toLong())
        val tilgang = response.body as Tilgang
        Assert.assertTrue(tilgang.isHarTilgang)
    }

    private fun assertAccessDenied(response: ResponseEntity<*>, begrunnelse: String) {
        Assert.assertEquals(HTTP_STATUS_FORBIDDEN.toLong(), response.statusCodeValue.toLong())
        val tilgang = response.body as Tilgang
        Assert.assertFalse(tilgang.isHarTilgang)
        Assert.assertEquals(begrunnelse, tilgang.begrunnelse)
    }

    companion object {
        private const val INNVILG = true
        private const val NEKT = false
        private const val HTTP_STATUS_OK = 200
        private const val HTTP_STATUS_FORBIDDEN = 403
    }
}
