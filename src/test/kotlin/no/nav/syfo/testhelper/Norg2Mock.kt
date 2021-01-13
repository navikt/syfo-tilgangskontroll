package no.nav.syfo.testhelper

import no.nav.syfo.consumer.norg2.Enhetsstatus
import no.nav.syfo.consumer.norg2.NorgEnhet

const val ENHET_NR = "0101"
const val ENHET_NAVN = "Enhet"

fun generateNorgEnhet(enhetNr: String = ENHET_NR): NorgEnhet {
    return NorgEnhet(
        enhetNr = enhetNr,
        navn = ENHET_NAVN,
        status = Enhetsstatus.AKTIV.formattedName,
        aktiveringsdato = null,
        antallRessurser = null,
        enhetId = null,
        kanalstrategi = null,
        nedleggelsesdato = null,
        oppgavebehandler = null,
        orgNivaa = null,
        orgNrTilKommunaltNavKontor = null,
        organisasjonsnummer = null,
        sosialeTjenester = null,
        type = null,
        underAvviklingDato = null,
        underEtableringDato = null,
        versjon = null
    )
}
