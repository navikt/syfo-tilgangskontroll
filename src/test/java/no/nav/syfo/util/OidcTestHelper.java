package no.nav.syfo.util;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import no.nav.security.oidc.test.support.JwtTokenGenerator;

import java.text.ParseException;

public class OidcTestHelper {

    public static void loggInnVeilederMedOpenAM(OIDCRequestContextHolder oidcRequestContextHolder, String subject) {
        //OIDC-hack - legg til token og oidcclaims for en test-person
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(subject);
        String issuer = "intern";
        TokenContext tokenContext = new TokenContext(issuer, jwt.serialize());
        OIDCClaims oidcClaims = new OIDCClaims(jwt);
        OIDCValidationContext oidcValidationContext = new OIDCValidationContext();
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims);
        oidcRequestContextHolder.setOIDCValidationContext(oidcValidationContext);
    }

    public static void loggInnVeilederMedAzure(OIDCRequestContextHolder oidcRequestContextHolder, String veilederIdent) throws ParseException {
        JWTClaimsSet claimsSet = JWTClaimsSet.parse(lagVeilederClaimSet(veilederIdent));
        SignedJWT jwt = JwtTokenGenerator.createSignedJWT(claimsSet);
        String issuer = "veileder";
        TokenContext tokenContext = new TokenContext(issuer, jwt.serialize());
        OIDCClaims oidcClaims = new OIDCClaims(jwt);
        OIDCValidationContext oidcValidationContext = new OIDCValidationContext();
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims);
        oidcRequestContextHolder.setOIDCValidationContext(oidcValidationContext);

    }

    public static void loggUtAlle(OIDCRequestContextHolder oidcRequestContextHolder) {
        oidcRequestContextHolder.setOIDCValidationContext(null);
    }

    private static String lagVeilederClaimSet(String veilederIdent) {
        return "{\"sub\":\"subVerdi\",\"ver\":\"1.0\",\"NAVident\":\"" + veilederIdent + "\",\"aio\":\"aioVerdi\",\"amr\":[\"amr0\"],\"iss\":\"issUri\",\"onprem_sid\":\"onpremSidVerdi\"" +
                ",\"groups\":[\"gruppe1\",\"gruppe2\",\"gruppe3\"],\"oid\":\"oidVerdi\",\"uti\":\"utiVerdi\",\"given_name\":\"VIGGO\",\"nonce\":\"nonceVerdi\",\"tid\":\"tidVerdi\"" +
                ",\"aud\":\"audVerdi\",\"unique_name\":\"VIGGO.VEILEDER@nav.no\",\"upn\":\"VIGGO.VEILEDER@nav.no\",\"nbf\":123456789,\"name\":\"VIGGO VEILEDER\",\"exp\":123456789,\"" +
                "ipaddr\":\"127.0.0.1\",\"iat\":123456789,\"family_name\":\"VEILEDER\"}";
    }

}
