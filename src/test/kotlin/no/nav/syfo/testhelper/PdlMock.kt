package no.nav.syfo.testhelper

import no.nav.syfo.consumer.pdl.Adressebeskyttelse
import no.nav.syfo.consumer.pdl.Gradering
import no.nav.syfo.consumer.pdl.PdlHentPerson
import no.nav.syfo.consumer.pdl.PdlPerson

fun generateAdressebeskyttelse(
    gradering: Gradering = Gradering.UGRADERT
): Adressebeskyttelse {
    return Adressebeskyttelse(
        gradering = gradering
    ).copy()
}

fun generatePdlHentPerson(
    adressebeskyttelse: Adressebeskyttelse = generateAdressebeskyttelse()
): PdlHentPerson {
    return PdlHentPerson(
        hentPerson = PdlPerson(
            adressebeskyttelse = listOf(adressebeskyttelse)
        )
    ).copy()
}
