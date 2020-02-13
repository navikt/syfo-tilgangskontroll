package no.nav.syfo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Profile({"remote"})
public class CacheConfig {

    public static final String TILGANGTILBRUKER = "tilgangtilbruker";
    public static final String TILGANGTILTJENESTEN = "tilgangtiltjenesten";
    public static final String TILGANGTILENHET = "tilgangtilenhet";
    public static final String CACHENAME_EGENANSATT = "egenansatt";
    public static final String CACHENAME_ENHET_OVERORDNET_ENHETER = "enhetoverordnetenheter";
    public static final String CACHENAME_GEOGRAFISK_TILHORIGHET_ENHETER = "geografisktilhorighetenheter";
    public static final String CACHENAME_VEILEDER_ENHETER = "veilederenhet";
    public static final String CACHENAME_VEILEDER_LDAP = "ldapveilederrolle";
    public static final String CACHENAME_PERSON_INFO = "personinfo";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(12L));

        cacheConfigurations.put(TILGANGTILBRUKER, defaultConfig);
        cacheConfigurations.put(TILGANGTILTJENESTEN, defaultConfig);
        cacheConfigurations.put(TILGANGTILENHET, defaultConfig);
        cacheConfigurations.put(CACHENAME_EGENANSATT, defaultConfig);
        cacheConfigurations.put(CACHENAME_ENHET_OVERORDNET_ENHETER, defaultConfig);
        cacheConfigurations.put(CACHENAME_GEOGRAFISK_TILHORIGHET_ENHETER, defaultConfig);
        cacheConfigurations.put(CACHENAME_VEILEDER_ENHETER, defaultConfig);
        cacheConfigurations.put(CACHENAME_VEILEDER_LDAP, defaultConfig);
        cacheConfigurations.put(CACHENAME_PERSON_INFO, defaultConfig);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
