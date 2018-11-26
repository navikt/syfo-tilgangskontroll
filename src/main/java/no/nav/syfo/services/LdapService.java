package no.nav.syfo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

import static org.apache.commons.lang3.StringUtils.substringBetween;

@Service
public class LdapService {

    private final Hashtable<String, String> env = new Hashtable<>();
    private static String SEARCHBASE;

    @Autowired
    public LdapService(Environment springEnv) {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, springEnv.getRequiredProperty("ldap.url"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, springEnv.getRequiredProperty("ldap.username"));
        env.put(Context.SECURITY_CREDENTIALS, springEnv.getRequiredProperty("ldap.password"));

        SEARCHBASE = "OU=Users,OU=NAV,OU=BusinessUnits," + springEnv.getRequiredProperty("ldap.basedn");
    }

    public boolean harTilgang(String veilederUid, String rolle) {
        try {
            SearchControls searchCtrl = new SearchControls();
            searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> result = ldapContext().search(SEARCHBASE, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl);
            Attributes ldapAttributes = result.next().getAttributes();
            NamingEnumeration namingEnumeration = ldapAttributes.get("memberof").getAll();

            while (namingEnumeration.hasMore()) {
                if (hentRolleNavn(namingEnumeration.next().toString()).contains(rolle)) {
                    return true;
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private String hentRolleNavn(String ldapstreng) {
        return substringBetween(ldapstreng, "CN=", ",");
    }

    private LdapContext ldapContext() throws NamingException {
        return new InitialLdapContext(env, null);
    }

}
