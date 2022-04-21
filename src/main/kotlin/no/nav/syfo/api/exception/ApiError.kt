package no.nav.syfo.api.exception

data class ApiError(
    val status: Int,
    val message: String,
)
