package no.nav.syfo.consumer.ldap

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.domain.AdRolle
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*
import javax.naming.Context
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

@Service
class LdapService(
    private val cacheManager: CacheManager,
    private val graphApiConsumer: GraphApiConsumer,
) {
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

    fun harTilgang(
        veilederUid: String,
        adRolle: AdRolle,
    ): Boolean {
        val graphApiHasAccess = graphApiConsumer.hasAccess(
            veilederIdent = veilederUid,
            adRolle = adRolle,
        )
        val ldapHasAccess: Boolean = try {
            val cacheKey = "$veilederUid-${adRolle.rolle}"
            val cachedValue = cache().get(cacheKey)?.get() as Boolean?
            if (cachedValue != null) {
                cachedValue
            } else {
                val hasAccess = ldapHasAccess(
                    rolle = adRolle.rolle,
                    veilederUid = veilederUid,
                )
                cache().put(cacheKey, hasAccess)
                hasAccess
            }
        } catch (e: NamingException) {
            throw RuntimeException(e)
        }

        if (graphApiHasAccess != ldapHasAccess) {
            log.info("ACCESS-TRACE: graphApiHasAccess=$graphApiHasAccess is not equal to ldapHasAccess=$ldapHasAccess, role=${adRolle.rolle}, veilederUid=${veilederUid}")
        }
        return ldapHasAccess
    }

    fun ldapHasAccess(
        veilederUid: String,
        rolle: String,
    ): Boolean {
        val searchCtrl = SearchControls()
        searchCtrl.searchScope = SearchControls.SUBTREE_SCOPE

        val result =
            ldapContext().search(SEARCHBASE, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl)
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
            log.error("SearchResult from LDAP was empty for veileder {}", veilederUid)
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

    private fun cache(): Cache {
        return cacheManager.getCache(CacheConfig.CACHENAME_VEILEDER_LDAP)!!
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LdapService::class.java)
        private var SEARCHBASE: String? = null
    }
}
