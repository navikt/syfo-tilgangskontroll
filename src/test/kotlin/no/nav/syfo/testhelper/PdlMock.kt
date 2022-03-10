package no.nav.syfo.testhelper

import no.nav.syfo.consumer.pdl.*

fun generateAdressebeskyttelse(
    gradering: Gradering = Gradering.UGRADERT
) = Adressebeskyttelse(
    gradering = gradering
).copy()

fun generatePdlHentPerson(
    adressebeskyttelse: Adressebeskyttelse = generateAdressebeskyttelse()
) = PdlHentPerson(
    hentPerson = PdlPerson(
        adressebeskyttelse = listOf(adressebeskyttelse)
    )
).copy()
