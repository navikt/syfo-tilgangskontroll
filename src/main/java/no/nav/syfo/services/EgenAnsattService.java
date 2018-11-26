package no.nav.syfo.services;

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class EgenAnsattService {

    @Inject
    private EgenAnsattV1 egenAnsattV1;

    boolean erEgenAnsatt(String fnr) {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt();
    }
}
