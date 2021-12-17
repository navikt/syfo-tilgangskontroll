package no.nav.syfo.testhelper

import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.domain.AdRolle
import org.mockito.Mockito

object GraphApiUtil {
    @JvmStatic
    fun mockAdRolle(
        graphApiConsumer: GraphApiConsumer,
        veilederIdent: String,
        innvilget: Boolean,
        vararg adRolleList: AdRolle,
    ) {
        Mockito.reset(graphApiConsumer)
        for (rolle in adRolleList) {
            Mockito.`when`(
                graphApiConsumer.hasAccess(
                    veilederIdent = veilederIdent,
                    adRolle = rolle,
                )
            ).thenReturn(innvilget)
        }
    }
}
