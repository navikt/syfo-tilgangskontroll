package no.nav.syfo.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

public class SyfoTilgangskontrollApplication extends ResourceConfig {

    public SyfoTilgangskontrollApplication() {
        packages("no.nav.syfo.rest");
        register(JacksonJaxbJsonProvider.class);
    }
}
