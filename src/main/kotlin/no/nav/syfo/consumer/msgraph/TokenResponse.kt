package no.nav.syfo.consumer.msgraph

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse (val access_token: String) : Serializable
