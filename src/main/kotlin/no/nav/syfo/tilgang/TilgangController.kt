package no.nav.syfo.tilgang

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCClaim.NAVIDENT
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.api.auth.OIDCIssuer.VEILEDERAZURE
import no.nav.syfo.api.auth.OIDCUtil.getConsumerClientId
import no.nav.syfo.api.auth.OIDCUtil.getSubjectFromAzureOIDCToken
import no.nav.syfo.api.auth.OIDCUtil.getTokenFromAzureOIDCToken
import no.nav.syfo.consumer.msgraph.TokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.getPersonIdent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/tilgang"])
class TilgangController @Autowired constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val metric: Metric,
    private val tilgangService: TilgangService,
    private val tokenConsumer: TokenConsumer
) {
    @GetMapping(path = ["/syfo"])
    @ProtectedWithClaims(issuer = AZURE)
    fun tilgangTilTjenestenViaAzure(): ResponseEntity<*> {
        val veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT)
        return sjekkTilgangTilTjenesten(veilederId)
    }

    @GetMapping(path = ["/bruker"])
    @ProtectedWithClaims(issuer = AZURE)
    fun tilgangTilBrukerViaAzure(@RequestParam fnr: String): ResponseEntity<*> {
        val veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT)
        return sjekktilgangTilBruker(veilederId, fnr)
    }

    @PostMapping(path = ["/brukere"])
    @ProtectedWithClaims(issuer = AZURE)
    fun tilgangTilBrukereViaAzure(@RequestBody fnrList: List<String>): ResponseEntity<*> {
        val veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT)
        return sjekkTilgangTilBrukere(veilederId, fnrList)
    }

    @GetMapping(path = ["/enhet"])
    @ProtectedWithClaims(issuer = AZURE)
    fun tilgangTilEnhet(@RequestParam enhet: String): ResponseEntity<*> {
        val veilederId = getSubjectFromAzureOIDCToken(contextHolder, AZURE, NAVIDENT)
        if (!enhet.matches(Regex("\\d{4}$"))) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("enhet paramater must be at least four digits long")
        val tilgang = tilgangService.sjekkTilgangTilEnhet(veilederId, enhet)
        return lagRespons(tilgang)
    }

    @GetMapping(path = ["/navident/syfo"])
    @ProtectedWithClaims(issuer = VEILEDERAZURE)
    fun accessToSYFOViaAzure(): ResponseEntity<*> {
        val veilederId = tokenConsumer.getSubjectFromMsGraph(getTokenFromAzureOIDCToken(contextHolder))
        return sjekkTilgangTilTjenesten(veilederId)
    }

    @GetMapping(path = ["/navident/bruker/{fnr}"])
    @ProtectedWithClaims(issuer = VEILEDERAZURE)
    fun accessToPersonViaAzure(@PathVariable fnr: String): ResponseEntity<*> {
        val veilederId = tokenConsumer.getSubjectFromMsGraph(getTokenFromAzureOIDCToken(contextHolder))
        val consumerClientId = getConsumerClientId(contextHolder)
        return sjekktilgangTilBruker(veilederId, fnr, consumerClientId)
    }

    @GetMapping(path = ["/navident/person"])
    @ProtectedWithClaims(issuer = VEILEDERAZURE)
    fun accessToPersonIdentViaAzure(
        @RequestHeader headers: MultiValueMap<String, String>,
    ): ResponseEntity<*> {
        val requestedPersonIdent = headers.getPersonIdent() ?: throw IllegalArgumentException("Did not find a PersonIdent in request headers")

        val veilederId = tokenConsumer.getSubjectFromMsGraph(getTokenFromAzureOIDCToken(contextHolder))
        val consumerClientId = getConsumerClientId(contextHolder)
        return sjekktilgangTilBruker(veilederId, requestedPersonIdent, consumerClientId)
    }

    @PostMapping(path = ["/navident/brukere"])
    @ProtectedWithClaims(issuer = VEILEDERAZURE)
    fun accessToPersonListViaAzure(
        @RequestBody personIdentList: List<String>,
    ): ResponseEntity<*> {
        val veilederId = tokenConsumer.getSubjectFromMsGraph(getTokenFromAzureOIDCToken(contextHolder))
        return sjekkTilgangTilBrukere(veilederId, personIdentList)
    }

    @GetMapping(path = ["/navident/enhet/{enhetNr}"])
    @ProtectedWithClaims(issuer = VEILEDERAZURE)
    fun accessToEnhet(
        @PathVariable enhetNr: String,
    ): ResponseEntity<*> {
        val veilederId = tokenConsumer.getSubjectFromMsGraph(getTokenFromAzureOIDCToken(contextHolder))
        if (!enhetNr.matches(Regex("\\d{4}$"))) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("enhet paramater must be at least four digits long")
        val tilgang = tilgangService.sjekkTilgangTilEnhet(veilederId, enhetNr)
        return lagRespons(tilgang)
    }

    private fun sjekkTilgangTilTjenesten(veilederId: String): ResponseEntity<*> {
        val tilgang = tilgangService.sjekkTilgangTilTjenesten(veilederId)
        return lagRespons(tilgang)
    }

    private fun sjekktilgangTilBruker(
        veilederId: String,
        fnr: String,
        consumerClientId: String = ""
    ): ResponseEntity<*> {
        val tilgang = tilgangService.sjekkTilgangTilBruker(veilederId, fnr, consumerClientId)
        return lagRespons(tilgang)
    }

    private fun trySjekkTilgangTilBruker(veilederId: String, fnr: String): Boolean {
        return try {
            tilgangService.sjekkTilgangTilBruker(veilederId, fnr).harTilgang
        } catch (e: RuntimeException) {
            log.error("Uventet feil ved sjekk av tilgang til bruker i liste: {} : {}", e.toString(), e.message, e)
            metric.countEvent("access_persons_person_error")
            false
        }
    }

    private fun sjekkTilgangTilBrukere(veilederId: String, fnrList: List<String>): ResponseEntity<*> {
        val filtrerteBrukereMedTilgang = fnrList
            .filter { fnr: String ->
                trySjekkTilgangTilBruker(veilederId, fnr)
            }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(filtrerteBrukereMedTilgang)
    }

    private fun lagRespons(tilgang: Tilgang): ResponseEntity<*> {
        return if (tilgang.harTilgang) ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(tilgang) else ResponseEntity.status(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(tilgang)
    }

    companion object {
        private val log = LoggerFactory.getLogger(TilgangController::class.java)
    }
}
