package no.nav.syfo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;

import static java.util.Arrays.asList;

@Configuration
@EnableCaching
@Profile({"remote"})
public class CacheConfig {

    public static final String TILGANGTILBRUKER = "tilgangtilbruker";
    public static final String TILGANGTILTJENESTEN = "tilgangtiltjenesten";
    public static final String TILGANGTILENHET = "tilgangtilenhet";
    public static final String CACHENAME_EGENANSATT = "egenansatt";
    public static final String CACHENAME_PERSON_INFO = "personinfo";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(asList(
                new ConcurrentMapCache(TILGANGTILBRUKER),
                new ConcurrentMapCache(TILGANGTILENHET),
                new ConcurrentMapCache(TILGANGTILTJENESTEN),
                new ConcurrentMapCache(CACHENAME_EGENANSATT),
                new ConcurrentMapCache(CACHENAME_PERSON_INFO)
        ));
        return cacheManager;
    }
}
