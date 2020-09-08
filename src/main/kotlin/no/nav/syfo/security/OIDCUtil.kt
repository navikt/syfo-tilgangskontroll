package no.nav.syfo.security

import no.nav.security.oidc.OIDCConstants
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import java.text.ParseException

object OIDCUtil {
    @JvmStatic
    fun getSubjectFromAzureOIDCToken(contextHolder: OIDCRequestContextHolder, issuerName: String?, claimName: String?): String {
        val context = contextHolder
                .getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
        return try {
            context.getClaims(issuerName).claimSet.getStringClaim(claimName)
        } catch (e: ParseException) {
            throw RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)")
        }
    }
}