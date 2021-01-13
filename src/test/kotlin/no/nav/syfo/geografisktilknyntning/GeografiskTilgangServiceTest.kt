package no.nav.syfo.geografisktilknyntning

import no.nav.syfo.axsys.AxsysConsumer
import no.nav.syfo.axsys.AxsysEnhet
import no.nav.syfo.behandlendeenhet.BehandlendeEnhetConsumer
import no.nav.syfo.domain.AdRoller
import no.nav.syfo.geografisktilknytning.GeografiskTilgangService
import no.nav.syfo.ldap.LdapService
import no.nav.syfo.norg2.NorgConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.testhelper.UserConstants.NAV_ENHET_NAVN
import no.nav.syfo.testhelper.UserConstants.PERSON_FNR
import no.nav.syfo.testhelper.generateBehandlendeEnhet
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GeografiskTilgangServiceTest {
    @Mock
    private lateinit var axsysConsumer: AxsysConsumer

    @Mock
    private lateinit var behandlendeEnhetConsumer: BehandlendeEnhetConsumer

    @Mock
    private lateinit var ldapService: LdapService

    @Mock
    private lateinit var norgConsumer: NorgConsumer

    @Mock
    private lateinit var pdlConsumer: PdlConsumer

    @InjectMocks
    private lateinit var geografiskTilgangService: GeografiskTilgangService

    @Before
    fun setup() {
        Mockito.`when`(norgConsumer.getNAVKontorForGT(GEOGRAFISK_TILKNYTNING)).thenReturn(
            BRUKERS_ENHET
        )
    }

    @Test
    fun nasjonalTilgangGirTilgang() {
        Mockito.`when`(ldapService.harTilgang(VEILEDER_UID, AdRoller.NASJONAL.rolle)).thenReturn(true)
        Assertions.assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, PERSON_FNR)).isTrue
    }

    @Test
    fun utvidetTilNasjonalTilgangGirTilgang() {
        Mockito.`when`(ldapService.harTilgang(VEILEDER_UID, AdRoller.UTVIDBAR_TIL_NASJONAL.rolle)).thenReturn(true)
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
        Mockito.`when`(ldapService.harTilgang(VEILEDER_UID, AdRoller.REGIONAL.rolle)).thenReturn(true)
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
        private const val GEOGRAFISK_TILKNYTNING = "brukersPostnummer"
        private const val GEOGRAFISK_TILKNYTNING_UTLAND = "SWE"
    }
}
