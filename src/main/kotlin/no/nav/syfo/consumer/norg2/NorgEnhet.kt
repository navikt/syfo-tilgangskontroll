package no.nav.syfo.consumer.norg2

import java.io.Serializable

data class NorgEnhet(
    val aktiveringsdato: String?,
    val antallRessurser: String?,
    val enhetId: String?,
    val enhetNr: String,
    val kanalstrategi: String?,
    val navn: String,
    val nedleggelsesdato: String?,
    val oppgavebehandler: String?,
    val orgNivaa: String?,
    val orgNrTilKommunaltNavKontor: String?,
    val organisasjonsnummer: String?,
    val sosialeTjenester: String?,
    val status: String,
    val type: String?,
    val underAvviklingDato: String?,
    val underEtableringDato: String?,
    val versjon: String?,
) : Serializable

enum class Enhetsstatus(val formattedName: String) {
    UNDER_ETABLERING("Under etablering"),
    AKTIV("Aktiv"),
    UNDER_AVVIKLING("Under avvikling"),
    NEDLAGT("Nedlagt");
}
