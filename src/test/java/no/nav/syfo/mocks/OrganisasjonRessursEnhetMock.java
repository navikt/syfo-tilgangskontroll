package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;

public class OrganisasjonRessursEnhetMock implements OrganisasjonRessursEnhetV1 {

    public static final String VIGGO_VEILEDER = "Z0VIGGO";
    private static final WSEnhet _0330 = new WSEnhet()
            .withEnhetId("0330")
            .withNavn("0330");
    private static final WSEnhet _1814 = new WSEnhet()
            .withEnhetId("1814")
            .withNavn("1814");

    @Override
    public void ping() {

    }

    @Override
    public WSHentEnhetListeResponse hentEnhetListe(WSHentEnhetListeRequest wsHentEnhetListeRequest) throws HentEnhetListeUgyldigInput, HentEnhetListeRessursIkkeFunnet {
        String ressursId = wsHentEnhetListeRequest.getRessursId();
        if (VIGGO_VEILEDER.equals(ressursId)){
            return new WSHentEnhetListeResponse()
                    .withEnhetListe(
                            _0330,
                            _1814
                    );
        } else {
            return new WSHentEnhetListeResponse()
                    .withEnhetListe(
                            _0330
                    );
        }
    }

    @Override
    public WSHentRessursIdListeResponse hentRessursIdListe(WSHentRessursIdListeRequest wsHentRessursIdListeRequest) throws HentRessursIdListeEnhetikkefunnet, HentRessursIdListeUgyldigInput {
        return null;
    }
}
