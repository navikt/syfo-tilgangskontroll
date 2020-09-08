package no.nav.syfo.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.metric.Metric;
import no.nav.syfo.services.TilgangService;
import no.nav.syfo.security.TokenConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.syfo.security.OIDCClaim.NAVIDENT;
import static no.nav.syfo.security.OIDCIssuer.AZURE;
import static no.nav.syfo.security.OIDCIssuer.VEILEDERAZURE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static no.nav.syfo.security.OIDCUtil.getSubjectFromAzureOIDCToken;

@Slf4j
@RestController
@RequestMapping(value = "/api/tilgang")
public class TilgangRessurs {

    private OIDCRequestContextHolder contextHolder;

    private Metric metric;

    private TilgangService tilgangService;

    private TokenConsumer tokenConsumer;

    @Autowired
    public TilgangRessurs(
            OIDCRequestContextHolder contextHolder,
            Metric metric,
            TilgangService tilgangService,
            TokenConsumer tokenConsumer
    ) {
        this.contextHolder = contextHolder;
        this.metric = metric;
        this.tilgangService = tilgangService;
        this.tokenConsumer = tokenConsumer;
    }

    @GetMapping(path = "/syfo")
    @ProtectedWithClaims(issuer = AZURE)
    public ResponseEntity tilgangTilTjenestenViaAzure() {
        String veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT);
        return sjekkTilgangTilTjenesten(veilederId);
    }

    @GetMapping(path = "/bruker")
    @ProtectedWithClaims(issuer = AZURE)
    public ResponseEntity tilgangTilBrukerViaAzure(@RequestParam String fnr) {
        String veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT);
        return sjekktilgangTilBruker(veilederId, fnr);
    }

    @PostMapping(path = "/brukere")
    @ProtectedWithClaims(issuer = AZURE)
    public ResponseEntity tilgangTilBrukereViaAzure(@RequestBody List<String> fnrList) {
        String veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT);
        return sjekkTilgangTilBrukere(veilederId, fnrList);
    }

    @GetMapping(path = "/enhet")
    @ProtectedWithClaims(issuer = AZURE)
    public ResponseEntity tilgangTilEnhet(@RequestParam String enhet) {
        String veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT);
        if (!enhet.matches("\\d{4}$"))
            return status(BAD_REQUEST)
                    .body("enhet paramater must be at least four digits long");
        Tilgang tilgang = tilgangService.sjekkTilgangTilEnhet(veilederId, enhet);
        return lagRespons(tilgang);
    }

    @GetMapping(path = "/navident/bruker/{fnr}")
    @ProtectedWithClaims(issuer = VEILEDERAZURE)
    public ResponseEntity accessToPersonViaAzure(@PathVariable String fnr) {
        String veilederId = tokenConsumer.getSubjectFromMsGraph(contextHolder);
        return sjekktilgangTilBruker(veilederId, fnr);
    }

    private ResponseEntity sjekkTilgangTilTjenesten(String veilederId) {
        Tilgang tilgang = tilgangService.sjekkTilgangTilTjenesten(veilederId);
        return lagRespons(tilgang);
    }

    private ResponseEntity sjekktilgangTilBruker(String veilederId, String fnr) {
        Tilgang tilgang = tilgangService.sjekkTilgangTilBruker(veilederId, fnr);
        return lagRespons(tilgang);
    }

    private ResponseEntity sjekkTilgangTilBrukere(String veilederId, List<String> fnrList) {
        List<String> filtrerteBrukereMedTilgang = fnrList
                .stream()
                .filter((fnr) -> {
                    try {
                        return tilgangService.sjekkTilgangTilBruker(veilederId, fnr).isHarTilgang();
                    } catch (RuntimeException e) {
                        log.error("Uventet feil ved sjekk av tilgang til bruker i liste: {} : {}", e.toString(), e.getMessage(), e);
                        metric.countEvent("access_persons_person_error");
                        return false;
                    }
                })
                .collect(Collectors.toList());
        return ok()
                .contentType(APPLICATION_JSON)
                .body(filtrerteBrukereMedTilgang);
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
