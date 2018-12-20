package no.nav.syfo.mocks;

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;

public class EgenansattMock implements EgenAnsattV1 {



    @Override
    public WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse hentErEgenAnsattEllerIFamilieMedEgenAnsatt(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest wsHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest) {
        String ident = wsHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest.getIdent();
        return new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse().withEgenAnsatt(PersonMock.ERIK_EGENANSATT_BRUKER.equals(ident));
    }

    @Override
    public void ping() {

    }
}
