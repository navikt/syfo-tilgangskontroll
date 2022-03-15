package no.nav.syfo.api.auth

import no.nav.security.token.support.core.context.TokenValidationContextHolder

object OIDCUtil {
    @JvmStatic
    fun getConsumerClientId(contextHolder: TokenValidationContextHolder): String {
        return contextHolder.tokenValidationContext.getClaims(OIDCIssuer.VEILEDERAZURE).getStringClaim("azp")
            ?: throw IllegalArgumentException("Claim AZP was not found in token")
    }
}

fun getNAVIdentFromOBOToken(contextHolder: TokenValidationContextHolder): String? {
    val context = contextHolder.tokenValidationContext
    return context.getClaims(OIDCIssuer.VEILEDERAZURE).getStringClaim(OIDCClaim.NAVIDENT)
}

fun TokenValidationContextHolder.getToken(): String =
    this.tokenValidationContext.getJwtToken(OIDCIssuer.VEILEDERAZURE).tokenAsString
