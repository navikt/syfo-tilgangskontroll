package no.nav.syfo.consumer.pdl

data class PdlPersonRequest(
    val query: String,
    val variables: PdlPersonRequestVariables,
)

data class PdlPersonRequestVariables(
    val ident: String,
)
