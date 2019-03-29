package no.nav.syfo.api;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.PersonInfo;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.security.OIDCUtil;
import no.nav.syfo.services.PersonService;
import no.nav.syfo.services.TilgangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static no.nav.syfo.security.OIDCClaim.NAVIDENT;
import static no.nav.syfo.security.OIDCIssuer.AZURE;
import static no.nav.syfo.security.OIDCIssuer.INTERN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(value = "/api/tilgang")
public class TilgangRessurs {

    private OIDCRequestContextHolder contextHolder;

    private TilgangService tilgangService;

    private PersonService personService;

    @Autowired
    public TilgangRessurs(OIDCRequestContextHolder contextHolder, TilgangService tilgangService, PersonService personService) {
        this.contextHolder = contextHolder;
        this.tilgangService = tilgangService;
        this.personService = personService;
    }

    @GetMapping(path = "/tilgangtilbruker")
    @ProtectedWithClaims(issuer = INTERN)
    public ResponseEntity tilgangTilBruker(@RequestParam String fnr) {
        String veilederId = OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN);
        return sjekkTilgangTilBruker(OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN), fnr);
    }

    @GetMapping(path = "/tilgangtiltjenesten")
    @ProtectedWithClaims(issuer = INTERN)
    public ResponseEntity tilgangTilTjenesten() {
        String veilederId = OIDCUtil.getSubjectFromOIDCToken(contextHolder, INTERN);
        return sjekkTilgangTilTjenesten(veilederId);
    }

    @GetMapping(path = "/syfo")
    @ProtectedWithClaims(issuer = AZURE)
    public ResponseEntity tilgangTilTjenestenViaAzure() {
        String veilederId = OIDCUtil.getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT);
        return sjekkTilgangTilTjenesten(veilederId);
    }

    @GetMapping(path = "/bruker")
    @ProtectedWithClaims(issuer = AZURE)
    public ResponseEntity tilgangTilBrukerViaAzure(@RequestParam String fnr) {
        String veilederId = OIDCUtil.getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT);
        return sjekkTilgangTilBruker(veilederId, fnr);
    }

    @GetMapping(path = "/enhet")
    @ProtectedWithClaims(issuer = AZURE)
    public ResponseEntity tilgangTilEnhet(@RequestParam String enhet) {
        String veilederId = OIDCUtil.getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT);
        if (!enhet.matches("\\d{4}$"))
            return status(BAD_REQUEST)
                    .body("enhet paramater must be at least four digits long");
        Tilgang tilgang = tilgangService.sjekkTilgangTilEnhet(veilederId, enhet);
        return lagRespons(tilgang);
    }

    private ResponseEntity sjekkTilgangTilTjenesten(String veilederId) {
        Tilgang tilgang = tilgangService.sjekkTilgangTilTjenesten(veilederId);
        return lagRespons(tilgang);
    }

    private ResponseEntity sjekkTilgangTilBruker(String veilederId, String fnr) {
        PersonInfo personInfo = personService.hentPersonInfo(fnr);
        Tilgang tilgang = tilgangService.sjekkTilgang(fnr, veilederId, personInfo);
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
