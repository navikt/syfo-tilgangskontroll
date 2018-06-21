package no.nav.syfo.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import javax.inject.Inject;

public class EgenAnsattService {

    @Inject
    private EgenAnsattV1 egenAnsattV1;

    //Default properties: https://github.com/Netflix/Hystrix/wiki/Configuration
    @HystrixCommand(
            commandKey = "erEgenAnsatt",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "20000"), //TODO Adjust timeout
                    @HystrixProperty(name = "fallback.enabled", value = "false")
            })
    public boolean erEgenAnsatt(String fnr) {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt();
    }

}