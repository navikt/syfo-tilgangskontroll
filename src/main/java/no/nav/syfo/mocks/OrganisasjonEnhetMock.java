package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.FinnNAVKontorUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.*;

public class OrganisasjonEnhetMock implements OrganisasjonEnhetV2 {
    @Override
    public WSHentFullstendigEnhetListeResponse hentFullstendigEnhetListe(WSHentFullstendigEnhetListeRequest request) {
        return null;
    }

    @Override
    public WSHentOverordnetEnhetListeResponse hentOverordnetEnhetListe(WSHentOverordnetEnhetListeRequest request) throws HentOverordnetEnhetListeEnhetIkkeFunnet {
        return null;
    }

    @Override
    public WSFinnNAVKontorResponse finnNAVKontor(WSFinnNAVKontorRequest request) throws FinnNAVKontorUgyldigInput {
        return null;
    }

    @Override
    public WSHentEnhetBolkResponse hentEnhetBolk(WSHentEnhetBolkRequest request) {
        return null;
    }

    @Override
    public void ping() {

    }
}
