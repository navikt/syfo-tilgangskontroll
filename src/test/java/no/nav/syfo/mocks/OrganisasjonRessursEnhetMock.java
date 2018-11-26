package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;

public class OrganisasjonRessursEnhetMock implements OrganisasjonRessursEnhetV1 {
    @Override
    public void ping() {

    }

    @Override
    public WSHentEnhetListeResponse hentEnhetListe(WSHentEnhetListeRequest wsHentEnhetListeRequest) throws HentEnhetListeUgyldigInput, HentEnhetListeRessursIkkeFunnet {
        return null;
    }

    @Override
    public WSHentRessursIdListeResponse hentRessursIdListe(WSHentRessursIdListeRequest wsHentRessursIdListeRequest) throws HentRessursIdListeEnhetikkefunnet, HentRessursIdListeUgyldigInput {
        return null;
    }
}
