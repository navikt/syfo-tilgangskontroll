package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;

public class PersonMock implements PersonV3 {

    public static final String BJARNE_BRUKER = "12345678910";
    public static final String BENGT_KODE6_BRUKER = "12345678966";
    public static final String BIRTE_KODE7_BRUKER = "12345678977";
    public static final String ERIK_EGENANSATT_BRUKER = "12345670330";

    public static final Kommune __0330 = new Kommune().withGeografiskTilknytning("0330");
    private static final Diskresjonskoder KODE_6 = new Diskresjonskoder().withValue("SPSF");
    private static final Diskresjonskoder KODE_7 = new Diskresjonskoder().withValue("SPFO");

    private static final HentGeografiskTilknytningResponse VANLIG_PERSON = new HentGeografiskTilknytningResponse()
            .withDiskresjonskode(null)
            .withGeografiskTilknytning(__0330);
    private static final HentGeografiskTilknytningResponse KODE6_PERSON = new HentGeografiskTilknytningResponse()
            .withDiskresjonskode(KODE_6)
            .withGeografiskTilknytning(__0330);
    private static final HentGeografiskTilknytningResponse KODE7_PERSON = new HentGeografiskTilknytningResponse()
            .withDiskresjonskode(KODE_7)
            .withGeografiskTilknytning(__0330);

    @Override
    public HentPersonResponse hentPerson(HentPersonRequest wsHentPersonRequest) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return null;
    }

    @Override
    public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest wsHentGeografiskTilknytningRequest) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
        String ident = ((PersonIdent) wsHentGeografiskTilknytningRequest.getAktoer()).getIdent().getIdent();
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
    public HentVergeResponse hentVerge(HentVergeRequest request) throws HentVergePersonIkkeFunnet, HentVergeSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentEkteskapshistorikkResponse hentEkteskapshistorikk(HentEkteskapshistorikkRequest request) throws HentEkteskapshistorikkPersonIkkeFunnet, HentEkteskapshistorikkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonerMedSammeAdresseResponse hentPersonerMedSammeAdresse(HentPersonerMedSammeAdresseRequest request) throws HentPersonerMedSammeAdresseIkkeFunnet, HentPersonerMedSammeAdresseSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentPersonhistorikkResponse hentPersonhistorikk(HentPersonhistorikkRequest request) throws HentPersonhistorikkPersonIkkeFunnet, HentPersonhistorikkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public HentSikkerhetstiltakResponse hentSikkerhetstiltak(HentSikkerhetstiltakRequest wsHentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public HentPersonnavnBolkResponse hentPersonnavnBolk(HentPersonnavnBolkRequest wsHentPersonnavnBolkRequest) {
        return null;
    }

}
