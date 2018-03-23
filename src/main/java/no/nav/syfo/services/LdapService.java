package no.nav.syfo.services;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.*;

//må bruke Hashtable i InitiallLdapContext dessverre.
@SuppressWarnings({"squid:S1149"})
public class LdapService {

    private static Hashtable<String, String> env = new Hashtable<>();

    @PostConstruct
    public static void setup() {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getProperty("LDAP_URL"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, getProperty("LDAP_USERNAME"));
        env.put(Context.SECURITY_CREDENTIALS, getProperty("LDAP_PASSWORD"));
    }


    public boolean harTilgang(String veilederUid, String rolle) {
        try {
            String searchbase = "OU=Users,OU=NAV,OU=BusinessUnits," + getProperty("LDAP_BASEDN");
            SearchControls searchCtrl = new SearchControls();
            searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> result = ldapContext().search(searchbase, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl);
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

    private static LdapContext ldapContext() throws NamingException {
        return new InitialLdapContext(env, null);
    }

    public void ping() {
        try {
            String searchbase = "OU=Users,OU=NAV,OU=BusinessUnits," + getProperty("LDAP_BASEDN");
            Attributes ldapAttributes = new BasicAttributes();
            ldapContext().search(searchbase, ldapAttributes);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
