package no.nav.syfo.api

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.domain.VeilederInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import no.nav.syfo.security.OIDCIssuer.VEILEDERAZURE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity.ok
import no.nav.syfo.security.TokenConsumer
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/veilederinfo"])
class VeilederInfoRessurs @Inject constructor(
        private val tokenConsumer: TokenConsumer,
        private val contextHolder: OIDCRequestContextHolder
) {
    @GetMapping(path = ["/ident"])
    @ProtectedWithClaims(issuer = VEILEDERAZURE)
    fun hentVeilederIdentFraToken() : ResponseEntity<VeilederInfo> {
        val veilederIdent = tokenConsumer.getSubjectFromMsGraph(contextHolder)
        return ok()
                .contentType(APPLICATION_JSON)
                .body(VeilederInfo(veilederIdent))

    }

}
