package no.nav.syfo.pdl

data class PdlGeografiskTilknytningRequest(
        val query: String,
        val variables: PdlGeografiskTilknytningRequestVariables
)

data class PdlGeografiskTilknytningRequestVariables(
        val ident: String
)
