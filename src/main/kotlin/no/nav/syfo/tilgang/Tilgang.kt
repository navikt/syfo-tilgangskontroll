package no.nav.syfo.tilgang

import java.io.Serializable

data class Tilgang(
    val harTilgang: Boolean = false,
) : Serializable
