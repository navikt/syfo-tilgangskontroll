package no.nav.syfo.api;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.security.OIDCUtil;
import no.nav.syfo.services.TilgangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static no.nav.syfo.security.OIDCIssuer.INTERN;

@RestController
@ProtectedWithClaims(issuer = INTERN)
@RequestMapping(value = "/api/tilgang")
public class TilgangRessurs {

    private OIDCRequestContextHolder contextHolder;

    private TilgangService tilgangService;

    @Autowired
    public TilgangRessurs(OIDCRequestContextHolder contextHolder, TilgangService tilgangService) {
        this.contextHolder = contextHolder;
        this.tilgangService = tilgangService;
    }

    @GetMapping(path = "/tilgangtiltjenesten")
    public ResponseEntity tilgangTilTjenesten() {
        String veilederId = OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN);
        Tilgang tilgang = tilgangService.sjekkTilgangTilTjenesten(veilederId);
        return lagRespons(tilgang);
    }

    @GetMapping(path = "/tilgangtilbruker")
    public ResponseEntity tilgangTilBruker(@RequestParam String fnr) {
        String veilederId = OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN);
        Tilgang tilgang = tilgangService.sjekkTilgang(fnr, veilederId);
        return lagRespons(tilgang);
    }

    @GetMapping(path = "/tilgangtilenhet")
    public ResponseEntity tilgangTilEnhet(@RequestParam String enhet) {
        String veilederId = OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN);
        if (!enhet.matches("\\d{4}$"))
            return status(BAD_REQUEST)
                    .body("enhet paramater must be at least four digits long");
        Tilgang tilgang = tilgangService.sjekkTilgangTilEnhet(veilederId, enhet);
        return lagRespons(tilgang);
    }

    private ResponseEntity lagRespons(Tilgang tilgang) {
        if (tilgang.isHarTilgang())
            return ok()
                    .contentType(APPLICATION_JSON)
                    .body(tilgang);
        return status(FORBIDDEN)
                .contentType(APPLICATION_JSON)
                .body(tilgang);
    }

}
