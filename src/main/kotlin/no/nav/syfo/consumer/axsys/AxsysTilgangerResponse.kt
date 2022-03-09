package no.nav.syfo.consumer.axsys

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class AxsysTilgangerResponse(
    val enheter: List<AxsysEnhet>,
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class AxsysEnhet(
    val enhetId: String,
    val navn: String,
) : Serializable
