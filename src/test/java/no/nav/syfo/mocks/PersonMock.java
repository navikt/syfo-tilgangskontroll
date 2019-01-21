package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.person.v3.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSDiskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSKommune;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;

public class PersonMock implements PersonV3 {

    public static final String BJARNE_BRUKER = "12345678910";
    public static final String BENGT_KODE6_BRUKER = "12345678966";
    public static final String BIRTE_KODE7_BRUKER = "12345678977";
    public static final String ERIK_EGENANSATT_BRUKER = "12345670330";

    private static final WSKommune __0330 = new WSKommune().withGeografiskTilknytning("0330");
    private static final WSDiskresjonskoder KODE_6 = new WSDiskresjonskoder().withValue("6");
    private static final WSDiskresjonskoder KODE_7 = new WSDiskresjonskoder().withValue("7");

    private static final WSHentGeografiskTilknytningResponse VANLIG_PERSON = new WSHentGeografiskTilknytningResponse()
            .withDiskresjonskode(null)
            .withGeografiskTilknytning(__0330);
    private static final WSHentGeografiskTilknytningResponse KODE6_PERSON = new WSHentGeografiskTilknytningResponse()
            .withDiskresjonskode(KODE_6)
            .withGeografiskTilknytning(__0330);
    private static final WSHentGeografiskTilknytningResponse KODE7_PERSON = new WSHentGeografiskTilknytningResponse()
            .withDiskresjonskode(KODE_7)
            .withGeografiskTilknytning(__0330);

    @Override
    public WSHentPersonResponse hentPerson(WSHentPersonRequest wsHentPersonRequest) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return null;
    }

    @Override
    public WSHentGeografiskTilknytningResponse hentGeografiskTilknytning(WSHentGeografiskTilknytningRequest wsHentGeografiskTilknytningRequest) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
        String ident = ((WSPersonIdent) wsHentGeografiskTilknytningRequest.getAktoer()).getIdent().getIdent();
        switch (ident){
            case BENGT_KODE6_BRUKER:
                return KODE6_PERSON;
            case BIRTE_KODE7_BRUKER:
                return KODE7_PERSON;
            default:
                return VANLIG_PERSON;
        }
    }

    @Override
    public WSHentSikkerhetstiltakResponse hentSikkerhetstiltak(WSHentSikkerhetstiltakRequest wsHentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public WSHentPersonnavnBolkResponse hentPersonnavnBolk(WSHentPersonnavnBolkRequest wsHentPersonnavnBolkRequest) {
        return null;
    }

}
