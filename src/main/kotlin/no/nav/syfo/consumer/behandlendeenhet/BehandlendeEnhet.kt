package no.nav.syfo.consumer.behandlendeenhet

import java.io.Serializable

data class BehandlendeEnhet(
    val enhetId: String,
    val navn: String
) : Serializable
