package no.nav.syfo.ldap

import no.nav.syfo.cache.CacheConfig
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*
import javax.naming.Context
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

@Service
class LdapService {
    private val env = Hashtable<String, String>()

    @Autowired
    fun LdapService(springEnv: Environment) {
        env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        env[Context.PROVIDER_URL] = springEnv.getRequiredProperty("ldap.url")
        env[Context.SECURITY_AUTHENTICATION] = "simple"
        env[Context.SECURITY_PRINCIPAL] = springEnv.getRequiredProperty("ldap.username")
        env[Context.SECURITY_CREDENTIALS] = springEnv.getRequiredProperty("ldap.password")
        SEARCHBASE = "OU=Users,OU=NAV,OU=BusinessUnits," + springEnv.getRequiredProperty("ldap.basedn")
    }

    @Cacheable(cacheNames = [CacheConfig.CACHENAME_VEILEDER_LDAP], key = "#veilederUid.concat(#rolle)", condition = "#veilederUid != null && #rolle != null")
    fun harTilgang(veilederUid: String, rolle: String): Boolean {
        try {
            val searchCtrl = SearchControls()
            searchCtrl.searchScope = SearchControls.SUBTREE_SCOPE
            val result = ldapContext().search(SEARCHBASE, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl)
            val searchResult = Optional.ofNullable(result.next())
            if (searchResult.isPresent) {
                val ldapAttributes = searchResult.get().attributes
                val namingEnumeration = ldapAttributes["memberof"].all
                while (namingEnumeration.hasMore()) {
                    if (hentRolleNavn(namingEnumeration.next().toString()).contains(rolle)) {
                        return true
                    }
                }
            } else {
                LOG.error("SearchResult from LDAP was empty for veileder {}", veilederUid)
            }
        } catch (e: NamingException) {
            throw RuntimeException(e)
        }
        return false
    }

    private fun hentRolleNavn(ldapstreng: String): String {
        return StringUtils.substringBetween(ldapstreng, "CN=", ",")
    }

    @Throws(NamingException::class)
    private fun ldapContext(): LdapContext {
        return InitialLdapContext(env, null)
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(LdapService::class.java)
        private var SEARCHBASE: String? = null
    }
}
