package no.nav.syfo.api;


import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.security.OIDCUtil;
import no.nav.syfo.services.TilgangService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static no.nav.syfo.security.OIDCIssuer.INTERN;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@RestController
@ProtectedWithClaims(issuer = INTERN)
@RequestMapping(value = "/api/tilgang")
public class TilgangRessurs {

    private OIDCRequestContextHolder contextHolder;

    private TilgangService tilgangService;

    @Inject
    public TilgangRessurs(OIDCRequestContextHolder contextHolder, TilgangService tilgangService) {
        this.contextHolder = contextHolder;
        this.tilgangService = tilgangService;
    }

    @GetMapping(path = "/tilgangtiltjenesten", produces = APPLICATION_JSON)
    public Response tilgangTilTjenesten() {
        String veilederId = OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN);
        if (tilgangService.harTilgangTilTjenesten(veilederId)) {
            return ok().build();
        } else {
            return status(FORBIDDEN)
                    .build();
        }
    }

    @GetMapping(path = "/tilgangtilbruker", produces = APPLICATION_JSON)
    public Response tilgangTilBruker(@QueryParam("fnr") String fnr) {
        if (isEmpty(fnr)) {
            return status(BAD_REQUEST)
                    .entity("fnr parameter is mandatory")
                    .build();
        } else {
            String veilederId = OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN);
            Tilgang tilgang = tilgangService.sjekkTilgang(fnr, veilederId);
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
