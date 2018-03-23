package no.nav.syfo.rest.ressurser;


import io.swagger.annotations.Api;
import no.nav.metrics.aspects.Count;
import no.nav.metrics.aspects.Timed;
import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.services.TilgangService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.apache.commons.lang3.StringUtils.*;

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
    public Response tilgangTilTjenesten() {
        if (tilgangService.harTilgangTilTjenesten()) {
            return ok().build();
        } else {
            return status(FORBIDDEN)
                    .build();
        }
    }

    @GET
    @Timed(name = "tilgangTilBruker")
    @Count(name = "tilgangTilBruker")
    @Path("/tilgangtilbruker")
    public Response tilgangTilBruker(@QueryParam("fnr") String fnr) {
        if (isEmpty(fnr)) {
            return status(BAD_REQUEST)
                    .entity("fnr parameter is mandatory")
                    .build();
        } else {
            Tilgang tilgang = tilgangService.sjekkTilgang(fnr);
            if (!tilgang.harTilgang) {
                return status(FORBIDDEN)
                        .entity(tilgang)
                        .type(APPLICATION_JSON)
                        .build();
            } else {
                return ok(new Tilgang().harTilgang(true)).build();
            }
        }
    }

}
