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
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
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
class PapirsykmeldingTilgangTest {
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
        logInVeilederWithAzure2(oidcRequestContextHolder, smregistreringClientId, VEILEDER_ID)
    }

    @AfterEach
    fun tearDown() {
        loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    fun `access to PersonIdent with Papirsykmelding granted for Smregistrering and NavIdent with rolle PAPIRSYKMELDING`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BJARNE_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to PersonIdent with Papirsykmelding denied if consuming application is not Smregistrering and NavIdent with rolle PAPIRSYKMELDING`() {
        loggUtAlle(oidcRequestContextHolder)
        logInVeilederWithAzure2(oidcRequestContextHolder, "", VEILEDER_ID)

        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BJARNE_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to PersonIdent with Papirsykmelding denied if consuming application is Smregistrering and NavIdent without rolle PAPIRSYKMELDING`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BJARNE_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to Kode6-personident with Papirsykmelding denied for Smregistrering and NAVIdent without rolle KODE6`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING),
        )
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BENGT_KODE6_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to Kode6-personident with Papirsykmelding granted for Smregistrering and NAVIdent with rolle SYFO, PAPIRSYKMELDING and old KODE6`() {
        loggUtAlle(oidcRequestContextHolder)
        logInVeilederWithAzure2(oidcRequestContextHolder, smregistreringClientId, VEILEDER_ID)

        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING, adRoller.OLD_KODE6),
        )
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BENGT_KODE6_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to Kode6-personident with Papirsykmelding granted for Smregistrering and NAVIdent with rolle SYFO, PAPIRSYKMELDING and new KODE6`() {
        loggUtAlle(oidcRequestContextHolder)
        logInVeilederWithAzure2(oidcRequestContextHolder, smregistreringClientId, VEILEDER_ID)

        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING, adRoller.KODE6),
        )
        Mockito.`when`(pdlConsumer.isKode6(ArgumentMatchers.any())).thenReturn(true)

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BENGT_KODE6_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to Kode7-personident with Papirsykmelding denied for Smregistrering and NAVIdent without either old or new rolle KODE7`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING),
        )
        Mockito.`when`(pdlConsumer.isKode7(ArgumentMatchers.any())).thenReturn(true)

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BIRTE_KODE7_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to Kode7-personident with Papirsykmelding granted for Smregistrering and NAVIdent with rolle SYFO, PAPIRSYKMELDING and old KODE7`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING, adRoller.OLD_KODE7),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BIRTE_KODE7_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to Kode7-personident with Papirsykmelding granted for Smregistrering and NAVIdent with rolle SYFO, PAPIRSYKMELDING and new KODE7`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING, adRoller.KODE7),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, BIRTE_KODE7_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to EgenAnsatt-personident with Papirsykmelding denied for Smregistrering and NAVIdent without either old or new rolle EGEN_ANSATT`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ERIK_EGENANSATT_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessDenied(response)
    }

    @Test
    fun `access to EgenAnsatt-personident with Papirsykmelding granted for Smregistrering and NAVIdent with rolle SYFO, PAPIRSYKMELDING and old EGEN_ANSATT`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING, adRoller.OLD_EGEN_ANSATT),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ERIK_EGENANSATT_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessOk(response)
    }

    @Test
    fun `access to EgenAnsatt-personident with Papirsykmelding granted for Smregistrering and NAVIdent with rolle SYFO, PAPIRSYKMELDING and new EGEN_ANSATT`() {
        mockAdRolle(
            graphApiConsumer = graphApiConsumerMock,
            veilederIdent = VEILEDER_ID,
            innvilget = INNVILG,
            adRolleList = arrayOf(adRoller.SYFO, adRoller.PAPIRSYKMELDING, adRoller.EGEN_ANSATT),
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ERIK_EGENANSATT_BRUKER)

        val response = tilgangController.accessToPersonIdentWithPapirsykmelding(headers)
        assertAccessOk(response)
    }

    private fun assertAccessOk(response: ResponseEntity<*>) {
        assertEquals(200, response.statusCodeValue)
        val tilgang = response.body as Tilgang
        assertTrue(tilgang.harTilgang)
    }

    private fun assertAccessDenied(response: ResponseEntity<*>) {
        assertEquals(403, response.statusCodeValue)
        val tilgang = response.body as Tilgang
        assertFalse(tilgang.harTilgang)
    }

    companion object {
        private const val INNVILG = true
    }
}
