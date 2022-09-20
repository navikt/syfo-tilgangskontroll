package no.nav.syfo.api

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.axsys.AxsysEnhet
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.consumer.norg2.NorgConsumer
import no.nav.syfo.consumer.pdl.*
import no.nav.syfo.consumer.skjermedepersoner.SkjermedePersonerPipConsumer
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.testhelper.GraphApiUtil.mockAdRolle
import no.nav.syfo.testhelper.OidcTestHelper.logInVeilederWithAzure2
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
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
import no.nav.syfo.tilgang.Tilgang
import no.nav.syfo.tilgang.TilgangController
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
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
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.text.ParseException

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

    @Autowired
    private lateinit var adRoller: AdRoller

    @Autowired
    private lateinit var oidcRequestContextHolder: TokenValidationContextHolder

    @Autowired
    private lateinit var graphApiConsumerMock: GraphApiConsumer

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
                )
            )
        )
        Mockito.`when`(
            norgConsumer.getNAVKontorForGT(
                GeografiskTilknytning(
                    type = GeografiskTilknytningType.BYDEL,
                    value = NAV_ENHETID_1,
                )
            )
        ).thenReturn(
            NAV_ENHETID_1
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(ArgumentMatchers.anyString())).thenReturn(
            GeografiskTilknytning(
                type = GeografiskTilknytningType.BYDEL,
                value = "0330",
            )
        )
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BJARNE_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BENGT_KODE6_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(BIRTE_KODE7_BRUKER)).thenReturn(false)
        Mockito.`when`(skjermedePersonerPipConsumer.erSkjermet(ERIK_EGENANSATT_BRUKER)).thenReturn(true)
        logInVeilederWithAzure2(oidcRequestContextHolder, "", VEILEDER_ID)
    }

    @AfterEach
    fun tearDown() {
        loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    fun accessToSYFOGranted() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
        val response = tilgangController.accessToSYFOViaAzure()
        assertAccessOk(response)
    }

    @Test
    fun accessToSYFODenied() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, NEKT, adRoller.SYFO)
        val response = tilgangController.accessToSYFOViaAzure()
        assertAccessDenied(response)
    }

    @Test
    fun `access to PersonIdent granted`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BJARNE_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to Kode6-personident denied`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BENGT_KODE6_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to Kode6-personident granted to veileder with Kode6 access`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO, adRoller.KODE6)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BENGT_KODE6_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to Kode6-personident granted for smregistrering and NAVIdent with Kode6 access`() {
        loggUtAlle(oidcRequestContextHolder)
        logInVeilederWithAzure2(oidcRequestContextHolder, smregistreringClientId, VEILEDER_ID)

        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO, adRoller.KODE6)
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BENGT_KODE6_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to Kode7-personident denied`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
        Mockito.`when`(pdlConsumer.isKode7(ArgumentMatchers.any())).thenReturn(true)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BIRTE_KODE7_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to Kode7-personident granted`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO, adRoller.KODE7)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BIRTE_KODE7_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to EgenAnsatt-personident denied`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ERIK_EGENANSATT_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to EgenAnsatt-personident granted`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO, adRoller.EGEN_ANSATT)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ERIK_EGENANSATT_BRUKER)
        val response = tilgangController.accessToPersonIdentViaAzure(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to enhet granted`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
        val response = tilgangController.accessToEnhet(NAV_ENHETID_1)
        assertAccessOk(response)
    }

    @Test
    fun `access to enhet denied`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
        val response = tilgangController.accessToEnhet(NAV_ENHETID_3)
        assertAccessDenied(response)
    }

    @Test
    fun `access to person list`() {
        mockAdRolle(graphApiConsumerMock, VEILEDER_ID, INNVILG, adRoller.SYFO)
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

        val response = tilgangController.accessToPersonListViaAzure(
            listOf(
                BJARNE_BRUKER,
                BENGT_KODE6_BRUKER,
                BIRTE_KODE7_BRUKER,
                ERIK_EGENANSATT_BRUKER
            )
        )
        assertEquals(200, response.statusCodeValue.toLong())
        assertEquals(listOf(BJARNE_BRUKER), response.body)
    }

    private fun assertAccessOk(response: ResponseEntity<*>) {
        assertEquals(HTTP_STATUS_OK.toLong(), response.statusCodeValue.toLong())
        val tilgang = response.body as Tilgang
        assertTrue(tilgang.harTilgang)
    }

    private fun assertAccessDenied(response: ResponseEntity<*>) {
        assertEquals(HTTP_STATUS_FORBIDDEN.toLong(), response.statusCodeValue.toLong())
        val tilgang = response.body as Tilgang
        assertFalse(tilgang.harTilgang)
    }

    companion object {
        private const val INNVILG = true
        private const val NEKT = false
        private const val HTTP_STATUS_OK = 200
        private const val HTTP_STATUS_FORBIDDEN = 403
    }
}
