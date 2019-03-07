package no.nav.syfo.security;

import static no.nav.security.oidc.OIDCConstants.*;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import java.text.ParseException;


public class OIDCUtil {

    public static String getSubjectFromOIDCToken(OIDCRequestContextHolder contextHolder, String issuerName) {
        OIDCValidationContext context = (OIDCValidationContext) contextHolder
                .getRequestAttribute(OIDC_VALIDATION_CONTEXT);
        return context.getClaims(issuerName).getClaimSet().getSubject();
    }

    public static String getSubjectFromAzureOIDCToken(OIDCRequestContextHolder contextHolder, String issuerName, String claimName) {
        OIDCValidationContext context = (OIDCValidationContext) contextHolder
                .getRequestAttribute(OIDC_VALIDATION_CONTEXT);
        try {
            return context.getClaims(issuerName).getClaimSet().getStringClaim(claimName);
        } catch (ParseException e) {
            throw new RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)");
        }
    }

}
