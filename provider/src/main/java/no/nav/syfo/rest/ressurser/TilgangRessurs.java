package no.nav.syfo.rest.ressurser;


import io.swagger.annotations.Api;
import no.nav.syfo.services.TilgangService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/tilgang")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "tilgang", description = "Endepunkt for sjekking av tilganger i DigiSyfo")
@Controller
public class TilgangRessurs {

    @Inject
    private TilgangService tilgangService;

    @GET
    public boolean harTilgang() {
        return tilgangService.harTilgangTilTjenesten();
    }
}
