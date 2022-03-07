package no.nav.syfo.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

class AdRolle(
    val name: String,
    val id: String,
    val rolle: String,
)

@Component
class AdRoller(
    @Value("\${role.kode6.id}") val kode6Id: String,
    @Value("\${role.kode7.id}") val kode7Id: String,
    @Value("\${role.syfo.id}") val syfoId: String,
    @Value("\${role.skjerming.id}") val skjermingId: String,
    @Value("\${role.nasjonal.id}") val nasjonalId: String,
    @Value("\${role.utvidbar.nasjonal.id}") val utvidbarNasjonalId: String,
    @Value("\${role.regional.id}") val regionalId: String,
    @Value("\${role.utvidbar.regional.id}") val utvidvarRegionalId: String,
    @Value("\${role.papirsykmelding.id}") val papirsykmeldingId: String,
) {
    val KODE6 = AdRolle(
        name = "KODE6",
        id = kode6Id,
        rolle = "0000-GA-GOSYS_KODE6",
    )
    val KODE7 = AdRolle(
        name = "KODE7",
        id = kode7Id,
        rolle = "0000-GA-GOSYS_KODE7"
    )
    val SYFO = AdRolle(
        name = "SYFO",
        id = syfoId,
        rolle = "0000-GA-SYFO-SENSITIV",
    )
    val EGEN_ANSATT = AdRolle(
        name = "EGEN_ANSATT",
        id = skjermingId,
        rolle = "0000-GA-GOSYS_UTVIDET",
    )
    val NASJONAL = AdRolle(
        name = "NASJONAL",
        id = nasjonalId,
        rolle = "0000-GA-GOSYS_NASJONAL",
    )
    val UTVIDBAR_TIL_NASJONAL = AdRolle(
        name = "UTVIDBAR_TIL_NASJONAL",
        id = utvidbarNasjonalId,
        rolle = "0000-GA-GOSYS_UTVIDBAR_TIL_NASJONAL",
    )
    val REGIONAL = AdRolle(
        name = "REGIONAL",
        id = regionalId,
        rolle = "0000-GA-GOSYS_REGIONAL",
    )
    val UTVIDBAR_TIL_REGIONAL = AdRolle(
        name = "UTVIDBAR_TIL_REGIONAL",
        id = utvidvarRegionalId,
        rolle = "0000-GA-GOSYS_UTVIDBAR_TIL_REGIONAL",
    )
    val PAPIRSYKMELDING = AdRolle(
        name = "PAPIRSYKMELDING",
        id = papirsykmeldingId,
        rolle = "0000-GA-papirsykmelding",
    )
}
