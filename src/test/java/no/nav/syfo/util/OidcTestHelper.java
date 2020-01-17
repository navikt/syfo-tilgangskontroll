package no.nav.syfo.util;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.context.*;
import no.nav.security.oidc.test.support.JwtTokenGenerator;

import java.text.ParseException;

import static no.nav.syfo.security.OIDCIssuer.*;

public class OidcTestHelper {

    public static void loggInnVeilederMedOpenAM(OIDCRequestContextHolder oidcRequestContextHolder, String subject) {
        //OIDC-hack - legg til token og oidcclaims for en test-person
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(subject);
        settOIDCValidationContext(oidcRequestContextHolder, jwt, INTERN);
    }

    public static void loggInnVeilederMedAzure(OIDCRequestContextHolder oidcRequestContextHolder, String veilederIdent) throws ParseException {
        JWTClaimsSet claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"" + veilederIdent + "\"}");
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(claimsSet);
        settOIDCValidationContext(oidcRequestContextHolder, jwt, AZURE);
    }

    public static void logInVeilederWithAzure2(OIDCRequestContextHolder oidcRequestContextHolder, String veilederIdent) throws ParseException {
        JWTClaimsSet claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"" + veilederIdent + "\"}");
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(claimsSet);
        settOIDCValidationContext(oidcRequestContextHolder, jwt, VEILEDERAZURE);
    }

    public static void loggUtAlle(OIDCRequestContextHolder oidcRequestContextHolder) {
        oidcRequestContextHolder.setOIDCValidationContext(null);
    }

    private static void settOIDCValidationContext(OIDCRequestContextHolder oidcRequestContextHolder, SignedJWT jwt, String issuer) {
        TokenContext tokenContext = new TokenContext(issuer, jwt.serialize());
        OIDCClaims oidcClaims = new OIDCClaims(jwt);
        OIDCValidationContext oidcValidationContext = new OIDCValidationContext();
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims);
        oidcRequestContextHolder.setOIDCValidationContext(oidcValidationContext);
    }

}
