package no.nav.syfo.consumer.pdl

import java.io.Serializable

data class GeografiskTilknytning(
    val type: GeografiskTilknytningType,
    val value: String?,
) : Serializable

enum class GeografiskTilknytningType {
    BYDEL,
    KOMMUNE,
    UTLAND,
    UDEFINERT,
}
