package no.nav.syfo.testhelper

import no.nav.syfo.consumer.behandlendeenhet.BehandlendeEnhet

fun generateBehandlendeEnhet(enhetNr: String = ENHET_NR): BehandlendeEnhet {
    return BehandlendeEnhet(
        enhetId = enhetNr,
        navn = ENHET_NAVN
    )
}
