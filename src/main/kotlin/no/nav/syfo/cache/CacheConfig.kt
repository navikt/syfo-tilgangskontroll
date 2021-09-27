package no.nav.syfo.cache

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration
import java.util.*

@Configuration
@EnableCaching
@Profile("remote")
class CacheConfig {
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val cacheConfigurations: MutableMap<String, RedisCacheConfiguration> = HashMap()
        val defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(12L))

        val shortConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(1L))

        val shortPDLConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(Duration.ofHours(1L))

        val longPDLConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(Duration.ofHours(12L))

        cacheConfigurations[TILGANGTILBRUKER] = defaultConfig
        cacheConfigurations[TILGANGTILTJENESTEN] = defaultConfig
        cacheConfigurations[TILGANGTILENHET] = defaultConfig
        cacheConfigurations[CACHENAME_AXSYS_ENHETER] = defaultConfig
        cacheConfigurations[CACHENAME_BEHANDLENDEENHET_FNR] = defaultConfig
        cacheConfigurations[CACHENAME_EGENANSATT] = shortConfig
        cacheConfigurations[CACHENAME_ENHET_OVERORDNET_ENHETER] = defaultConfig
        cacheConfigurations[CACHENAME_GEOGRAFISK_TILHORIGHET_ENHET] = defaultConfig
        cacheConfigurations[CACHENAME_VEILEDER_ENHETER] = defaultConfig
        cacheConfigurations[CACHENAME_VEILEDER_LDAP] = defaultConfig
        cacheConfigurations[CACHENAME_PDL_PERSON] = shortPDLConfig
        cacheConfigurations[CACHENAME_PDL_GEOGRAFISK_TILKNYTNING] = longPDLConfig

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    companion object {
        const val TILGANGTILBRUKER = "tilgangbruker"
        const val TILGANGTILTJENESTEN = "tilgangtjenesten"
        const val TILGANGTILENHET = "tilgangenhet"
        const val CACHENAME_AXSYS_ENHETER = "axsysenheter"
        const val CACHENAME_BEHANDLENDEENHET_FNR = "behandlendeenhetfnr"
        const val CACHENAME_EGENANSATT = "egenansatt"
        const val CACHENAME_ENHET_OVERORDNET_ENHETER = "enhetoverordnetenheter"
        const val CACHENAME_GEOGRAFISK_TILHORIGHET_ENHET = "geografisktilhorighetenhet"
        const val CACHENAME_VEILEDER_ENHETER = "veilederenhet"
        const val CACHENAME_VEILEDER_LDAP = "ldapveilederrolle"
        const val CACHENAME_PDL_PERSON = "pdlperson"
        const val CACHENAME_PDL_GEOGRAFISK_TILKNYTNING = "pdlgeografisk"
    }
}
