package no.nav.syfo.rest.ressurser;


import io.swagger.annotations.Api;
import no.nav.metrics.aspects.Count;
import no.nav.metrics.aspects.Timed;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.services.TilgangService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

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
    @Timed(name = "tilgangTilTjenesten")
    @Count(name = "tilgangTilTjenesten")
    @Path("/tilgangtiltjenesten")
    public boolean tilgangTilTjenesten() {
        return tilgangService.harTilgangTilTjenesten();
    }

    @GET
    @Timed(name = "tilgangTilBruker")
    @Count(name = "tilgangTilBruker")
    @Path("/tilgangtilbruker")
    public Tilgang tilgangTilBruker(@QueryParam("fnr") String fnr) {
        return tilgangService.sjekkTilgang(fnr);
    }

}
