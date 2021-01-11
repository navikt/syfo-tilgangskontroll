package no.nav.syfo.api

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.axsys.AxsysConsumer
import no.nav.syfo.axsys.AxsysEnhet
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.domain.Tilgang
import no.nav.syfo.norg2.NorgConsumer
import no.nav.syfo.pdl.Gradering
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.services.LdapService
import no.nav.syfo.skjermedepersoner.SkjermedePersonerPipConsumer
import no.nav.syfo.testhelper.UserConstants.BENGT_KODE6_BRUKER
import no.nav.syfo.testhelper.UserConstants.BIRTE_KODE7_BRUKER
import no.nav.syfo.testhelper.UserConstants.BJARNE_BRUKER
import no.nav.syfo.testhelper.UserConstants.ERIK_EGENANSATT_BRUKER
import no.nav.syfo.testhelper.UserConstants.NAV_ENHETID_1
import no.nav.syfo.testhelper.UserConstants.NAV_ENHETID_2
import no.nav.syfo.testhelper.UserConstants.NAV_ENHETID_3
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.generateAdressebeskyttelse
import no.nav.syfo.testhelper.generatePdlHentPerson
import no.nav.syfo.util.LdapUtil
import no.nav.syfo.util.OidcTestHelper
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
class TilgangRessursViaAzureComponentTest {
    @MockBean
    private lateinit var axsysConsumer: AxsysConsumer

    @MockBean
    private lateinit var norgConsumer: NorgConsumer

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @MockBean
    private lateinit var skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer

    @Autowired
    private lateinit var oidcRequestContextHolder: TokenValidationContextHolder

    @Autowired
    private lateinit var ldapServiceMock // TODO Forsøk å mocke selve ldap med f.eks spring.security.test
        : LdapService

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
        OidcTestHelper.loggInnVeilederMedAzure(oidcRequestContextHolder, VEILEDER_ID)
    }

    @After
    fun tearDown() {
        OidcTestHelper.loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    fun tilgangTilTjenestenInnvilget() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Assert.assertEquals(HTTP_STATUS_OK.toLong(), tilgangRessurs.tilgangTilTjenestenViaAzure().statusCodeValue.toLong())
    }

    @Test
    fun tilgangTilTjenestenNektet() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, NEKT, AdRoller.SYFO)
        Assert.assertEquals(HTTP_STATUS_FORBIDDEN.toLong(), tilgangRessurs.tilgangTilTjenestenViaAzure().statusCodeValue.toLong())
    }

    @Test
    fun tilgangTilBrukerInnvilget() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        val response = tilgangRessurs.tilgangTilBrukerViaAzure(BJARNE_BRUKER)
        assertTilgangOK(response)
    }

    @Test
    fun tilgangTilKode6BrukerNektet() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangRessurs.tilgangTilBrukerViaAzure(BENGT_KODE6_BRUKER)
        assertTilgangNektet(response, AdRoller.KODE6.name)
    }

    @Test
    fun tilgangTilKode6BrukerNektesAlltid() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.KODE6)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangRessurs.tilgangTilBrukerViaAzure(BENGT_KODE6_BRUKER)
        assertTilgangNektet(response, AdRoller.KODE6.name)
    }

    @Test
    fun tilgangTilKode7BrukerNektet() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode7(ArgumentMatchers.any())).thenReturn(true)
        val response = tilgangRessurs.tilgangTilBrukerViaAzure(BIRTE_KODE7_BRUKER)
        assertTilgangNektet(response, AdRoller.KODE7.name)
    }

    @Test
    fun tilgangTilKode7BrukerInnvilget() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.KODE7)
        val response = tilgangRessurs.tilgangTilBrukerViaAzure(BIRTE_KODE7_BRUKER)
        assertTilgangOK(response)
    }

    @Test
    fun tilgangTilBrukereSYFO() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode6(generatePdlHentPerson()))
            .thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6(generatePdlHentPerson(generateAdressebeskyttelse(Gradering.FORTROLIG))))
            .thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode6(generatePdlHentPerson(generateAdressebeskyttelse(Gradering.STRENGT_FORTROLIG))))
            .thenReturn(true)

        Mockito.`when`(pdlConsumer.isKode7(generatePdlHentPerson()))
            .thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode7(generatePdlHentPerson(generateAdressebeskyttelse(Gradering.STRENGT_FORTROLIG))))
            .thenReturn(false)
        Mockito.`when`(pdlConsumer.isKode7(generatePdlHentPerson(generateAdressebeskyttelse(Gradering.FORTROLIG))))
            .thenReturn(true)

        Mockito.`when`(pdlConsumer.person(BJARNE_BRUKER))
            .thenReturn(generatePdlHentPerson())
        Mockito.`when`(pdlConsumer.person(ERIK_EGENANSATT_BRUKER))
            .thenReturn(generatePdlHentPerson())
        Mockito.`when`(pdlConsumer.person(BENGT_KODE6_BRUKER))
            .thenReturn(generatePdlHentPerson(generateAdressebeskyttelse(Gradering.STRENGT_FORTROLIG)))
        Mockito.`when`(pdlConsumer.person(BIRTE_KODE7_BRUKER))
            .thenReturn(generatePdlHentPerson(generateAdressebeskyttelse(Gradering.FORTROLIG)))

        val response = tilgangRessurs.tilgangTilBrukereViaAzure(listOf(
            BJARNE_BRUKER,
            BENGT_KODE6_BRUKER,
            BIRTE_KODE7_BRUKER,
            ERIK_EGENANSATT_BRUKER
        ))
        Assert.assertEquals(200, response.statusCodeValue.toLong())
        Assert.assertEquals(listOf(BJARNE_BRUKER), response.body)
    }

    @Test
    fun tilgangTilEgenAnsattBrukerNektet() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        val response = tilgangRessurs.tilgangTilBrukerViaAzure(ERIK_EGENANSATT_BRUKER)
        assertTilgangNektet(response, AdRoller.EGEN_ANSATT.name)
    }

    @Test
    fun tilgangTilEgenAnsattBrukerInnvilget() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO, AdRoller.EGEN_ANSATT)
        val response = tilgangRessurs.tilgangTilBrukerViaAzure(ERIK_EGENANSATT_BRUKER)
        assertTilgangOK(response)
    }

    @Test
    fun tilgangTilEnhetInnvilget() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_ID)).thenReturn(listOf(
            AxsysEnhet(
                NAV_ENHETID_1,
                NAV_ENHET_NAVN
            )
        ))
        Assert.assertEquals(HTTP_STATUS_OK.toLong(), tilgangRessurs.tilgangTilEnhet(NAV_ENHETID_1).statusCodeValue.toLong())
    }

    @Test
    fun tilgangTilEnhetNektet() {
        LdapUtil.mockRoller(ldapServiceMock, VEILEDER_ID, INNVILG, AdRoller.SYFO)
        Assert.assertEquals(HTTP_STATUS_FORBIDDEN.toLong(), tilgangRessurs.tilgangTilEnhet(NAV_ENHETID_3).statusCodeValue.toLong())
    }

    private fun assertTilgangOK(response: ResponseEntity<*>) {
        Assert.assertEquals(HTTP_STATUS_OK.toLong(), response.statusCodeValue.toLong())
        val tilgang = response.body as Tilgang
        Assert.assertTrue(tilgang.isHarTilgang)
    }

    private fun assertTilgangNektet(response: ResponseEntity<*>, begrunnelse: String) {
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
