package no.nav.syfo.security;

import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;

public class OIDCUtil {

    public static String tokenFraOIDC(OIDCValidationContext contextHolder, String issuer) {
        return contextHolder.getToken(issuer).getIdToken();
    }

    public static String getSubjectFromOIDCToken(OIDCRequestContextHolder contextHolder, String issuerName) {
        OIDCValidationContext context = (OIDCValidationContext) contextHolder
                .getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT);
        return context.getClaims(issuerName).getClaimSet().getSubject();
    }
}
