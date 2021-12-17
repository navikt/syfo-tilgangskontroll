package no.nav.syfo.geografisktilknyntning

import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.axsys.AxsysEnhet
import no.nav.syfo.consumer.behandlendeenhet.BehandlendeEnhetConsumer
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.consumer.norg2.NorgConsumer
import no.nav.syfo.consumer.pdl.*
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.geografisktilknytning.GeografiskTilgangService
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN
import no.nav.syfo.testhelper.UserConstants.PERSON_FNR
import no.nav.syfo.testhelper.generateBehandlendeEnhet
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LocalApplication::class])
class GeografiskTilgangServiceTest {

    @MockBean
    private lateinit var axsysConsumer: AxsysConsumer

    @MockBean
    private lateinit var behandlendeEnhetConsumer: BehandlendeEnhetConsumer

    @MockBean
    private lateinit var graphApiConsumer: GraphApiConsumer

    @MockBean
    private lateinit var norgConsumer: NorgConsumer

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Autowired
    private lateinit var adRoller: AdRoller

    @Autowired
    private lateinit var geografiskTilgangService: GeografiskTilgangService

    @BeforeEach
    fun setup() {
        Mockito.`when`(norgConsumer.getNAVKontorForGT(GEOGRAFISK_TILKNYTNING)).thenReturn(
            BRUKERS_ENHET
        )
    }

    @Test
    fun nasjonalTilgangGirTilgang() {
        Mockito.`when`(graphApiConsumer.hasAccess(VEILEDER_UID, adRoller.NASJONAL)).thenReturn(true)
        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isTrue
    }

    @Test
    fun utvidetTilNasjonalTilgangGirTilgang() {
        Mockito.`when`(graphApiConsumer.hasAccess(VEILEDER_UID, adRoller.UTVIDBAR_TIL_NASJONAL)).thenReturn(true)
        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isTrue
    }

    @Test
    fun harTilgangHvisVeilederHarTilgangTilSammeEnhetSomBruker() {
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
            listOf(
                AxsysEnhet(
                    BRUKERS_ENHET,
                    NAV_ENHET_NAVN
                ),
                AxsysEnhet(
                    "enHeltAnnenEnhet",
                    NAV_ENHET_NAVN
                ))
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(PERSON_FNR)).thenReturn(GEOGRAFISK_TILKNYTNING)

        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isTrue
    }

    @Test
    fun harTilgangHvisVeilederHarTilgangTilSammeEnhetSomBrukerUtland() {
        val behandlendeEnhet = generateBehandlendeEnhet(BRUKERS_ENHET)
        Mockito.`when`(behandlendeEnhetConsumer.getBehandlendeEnhet(PERSON_FNR, null)).thenReturn(behandlendeEnhet)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
            listOf(
                AxsysEnhet(
                    BRUKERS_ENHET,
                    NAV_ENHET_NAVN
                ),
                AxsysEnhet(
                    "enHeltAnnenEnhet",
                    NAV_ENHET_NAVN
                ))
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(PERSON_FNR)).thenReturn(GEOGRAFISK_TILKNYTNING_UTLAND)

        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isTrue
    }

    @Test
    fun harIkkeTilgangHvisVeilederIkkeHarTilgangTilSammeEnhetSomBrukerUtland() {
        val behandlendeEnhet = generateBehandlendeEnhet(BRUKERS_ENHET)
        Mockito.`when`(behandlendeEnhetConsumer.getBehandlendeEnhet(PERSON_FNR, null)).thenReturn(behandlendeEnhet)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
            listOf(AxsysEnhet(
                "enHeltAnnenEnhet",
                NAV_ENHET_NAVN
            ))
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(PERSON_FNR)).thenReturn(GEOGRAFISK_TILKNYTNING_UTLAND)

        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isFalse
    }

    @Test
    fun harIkkeTilgangHvisVeilederIkkeHarTilgangTilSammeEnhetSomBruker() {
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
            listOf(AxsysEnhet(
                "enHeltAnnenEnhet",
                NAV_ENHET_NAVN
            ))
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(PERSON_FNR)).thenReturn(GEOGRAFISK_TILKNYTNING)

        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isFalse
    }

    @Test
    fun harTilgangHvisRegionalTilgangOgTilgangTilEnhetensFylkeskontor() {
        Mockito.`when`(graphApiConsumer.hasAccess(VEILEDER_UID, adRoller.REGIONAL)).thenReturn(true)
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
            listOf(AxsysEnhet(
                VEILEDERS_ENHET,
                NAV_ENHET_NAVN
            ))
        )
        Mockito.`when`(norgConsumer.getOverordnetEnhetListForNAVKontor(VEILEDERS_ENHET)).thenReturn(listOf(OVERORDNET_ENHET))
        Mockito.`when`(norgConsumer.getOverordnetEnhetListForNAVKontor(BRUKERS_ENHET)).thenReturn(listOf(OVERORDNET_ENHET))
        Mockito.`when`(pdlConsumer.geografiskTilknytning(PERSON_FNR)).thenReturn(GEOGRAFISK_TILKNYTNING)

        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isTrue
    }

    @Test
    fun harIkkeTilgangHvisTilgangTilEnhetensFylkeskontorMenIkkeRegionalTilgang() {
        Mockito.`when`(axsysConsumer.enheter(VEILEDER_UID)).thenReturn(
            listOf(AxsysEnhet(
                OVERORDNET_ENHET,
                NAV_ENHET_NAVN
            ))
        )
        Mockito.`when`(pdlConsumer.geografiskTilknytning(PERSON_FNR)).thenReturn(GEOGRAFISK_TILKNYTNING)

        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isFalse
    }

    companion object {
        private const val VEILEDER_UID = "Z999999"
        private const val BRUKERS_ENHET = "brukersEnhet"
        private const val VEILEDERS_ENHET = "veiledersEnhet"
        private const val OVERORDNET_ENHET = "fylkeskontor"
        private val GEOGRAFISK_TILKNYTNING =
            GeografiskTilknytning(
                type = GeografiskTilknytningType.KOMMUNE,
                value = "brukersPostnummer",
            )

        private val GEOGRAFISK_TILKNYTNING_UTLAND = GeografiskTilknytning(
            type = GeografiskTilknytningType.UTLAND,
            value = "SWE",
        )
    }
}
