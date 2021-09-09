package no.nav.syfo.consumer.azuread

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse (
    val access_token: String,
    val expires_in: Long,
) : Serializable
