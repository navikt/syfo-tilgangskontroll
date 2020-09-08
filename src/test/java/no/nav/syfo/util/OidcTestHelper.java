package no.nav.syfo.util;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.test.JwtTokenGenerator;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static no.nav.syfo.security.OIDCIssuer.AZURE;
import static no.nav.syfo.security.OIDCIssuer.VEILEDERAZURE;

public class OidcTestHelper {

    public static void loggInnVeilederMedAzure(TokenValidationContextHolder oidcRequestContextHolder, String veilederIdent) throws ParseException {
        JWTClaimsSet claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"" + veilederIdent + "\"}");
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(claimsSet);
        settOIDCValidationContext(oidcRequestContextHolder, jwt, AZURE);
    }

    public static void logInVeilederWithAzure2(TokenValidationContextHolder oidcRequestContextHolder, String veilederIdent) throws ParseException {
        JWTClaimsSet claimsSet = JWTClaimsSet.parse("{ }");
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(claimsSet);
        settOIDCValidationContext(oidcRequestContextHolder, jwt, VEILEDERAZURE);
    }

    public static void loggUtAlle(TokenValidationContextHolder oidcRequestContextHolder) {
        oidcRequestContextHolder.setTokenValidationContext(null);
    }

    private static void settOIDCValidationContext(TokenValidationContextHolder tokenValidationContextHolder, SignedJWT jwt, String issuer) {
        JwtToken jwtToken = new JwtToken(jwt.serialize());
        Map<String, JwtToken> issuerTokenMap = new HashMap<>();
        issuerTokenMap.put(issuer, jwtToken);
        TokenValidationContext tokenValidationContext = new TokenValidationContext(issuerTokenMap);
        tokenValidationContextHolder.setTokenValidationContext(tokenValidationContext);

    }

}
