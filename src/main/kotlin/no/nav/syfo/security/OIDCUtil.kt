package no.nav.syfo.security

import no.nav.security.token.support.core.context.TokenValidationContextHolder

object OIDCUtil {
    @JvmStatic
    fun getSubjectFromAzureOIDCToken(contextHolder: TokenValidationContextHolder, issuerName: String?, claimName: String?): String {
        val context = contextHolder.tokenValidationContext
        return context.getClaims(issuerName).getStringClaim(claimName)
    }

    @JvmStatic
    fun getTokenFromAzureOIDCToken(contextHolder: TokenValidationContextHolder): String {
        return contextHolder.tokenValidationContext.getJwtToken(OIDCIssuer.VEILEDERAZURE).tokenAsString
    }
}
