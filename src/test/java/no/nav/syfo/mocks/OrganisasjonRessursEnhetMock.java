package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;

public class OrganisasjonRessursEnhetMock implements OrganisasjonRessursEnhetV1 {

    public static final String VIGGO_VEILEDER = "Z0VIGGO";

    public static final String ENHET_1_ID = "0330";
    public static final String ENHET_2_ID = "1814";
    public static final String ENHET_3_ID = "0215";

    private static final WSEnhet WS_ENHET_1 = new WSEnhet()
            .withEnhetId(ENHET_1_ID)
            .withNavn(ENHET_1_ID);
    private static final WSEnhet WS_ENHET_2 = new WSEnhet()
            .withEnhetId(ENHET_2_ID)
            .withNavn(ENHET_2_ID);

    @Override
    public void ping() {

    }

    @Override
    public WSHentEnhetListeResponse hentEnhetListe(WSHentEnhetListeRequest wsHentEnhetListeRequest) throws HentEnhetListeUgyldigInput, HentEnhetListeRessursIkkeFunnet {
        String ressursId = wsHentEnhetListeRequest.getRessursId();
        if (VIGGO_VEILEDER.equals(ressursId)){
            return new WSHentEnhetListeResponse()
                    .withEnhetListe(
                            WS_ENHET_1,
                            WS_ENHET_2
                    );
        } else {
            return new WSHentEnhetListeResponse()
                    .withEnhetListe(
                            WS_ENHET_1
                    );
        }
    }

    @Override
    public WSHentRessursIdListeResponse hentRessursIdListe(WSHentRessursIdListeRequest wsHentRessursIdListeRequest) throws HentRessursIdListeEnhetikkefunnet, HentRessursIdListeUgyldigInput {
        return null;
    }
}
