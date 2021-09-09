package no.nav.syfo.consumer.azuread

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphResponse(val onPremisesSamAccountName: String) : Serializable
