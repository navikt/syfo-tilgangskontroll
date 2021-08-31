package no.nav.syfo.api.auth

import no.nav.security.token.support.core.context.TokenValidationContextHolder

object OIDCUtil {
    @JvmStatic
    fun getTokenFromAzureOIDCToken(contextHolder: TokenValidationContextHolder): String {
        return contextHolder.tokenValidationContext.getJwtToken(OIDCIssuer.VEILEDERAZURE).tokenAsString
    }

    @JvmStatic
    fun getConsumerClientId(contextHolder: TokenValidationContextHolder): String {
        return contextHolder.tokenValidationContext.getClaims(OIDCIssuer.VEILEDERAZURE).getStringClaim("azp")
    }
}

fun getNAVIdentFromOBOToken(contextHolder: TokenValidationContextHolder): String? {
    val context = contextHolder.tokenValidationContext
    return context.getClaims(OIDCIssuer.VEILEDERAZURE).getStringClaim(OIDCClaim.NAVIDENT)
}
