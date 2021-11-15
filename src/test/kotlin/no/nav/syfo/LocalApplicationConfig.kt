package no.nav.syfo

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_TOKENS
import no.nav.syfo.consumer.ldap.LdapService
import org.mockito.Mockito
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.*
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration
@Profile("local", "test")
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig {
    @Bean
    @Primary
    fun ldapServiceMock(): LdapService {
        return Mockito.mock(LdapService::class.java)
    }

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val cacheConfigurations: MutableMap<String, RedisCacheConfiguration> = HashMap()

        val shortConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(1L))

        cacheConfigurations[CACHENAME_TOKENS] = shortConfig

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
