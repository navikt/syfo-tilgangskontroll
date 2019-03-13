package no.nav.syfo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static java.util.Arrays.asList;
import static no.nav.syfo.services.TilgangService.*;

@Configuration
@EnableCaching
@Profile({"remote"})
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(asList(
                new ConcurrentMapCache(TILGANGTILBRUKER),
                new ConcurrentMapCache(TILGANGTILENHET),
                new ConcurrentMapCache(TILGANGTILTJENESTEN)
        ));
        return cacheManager;
    }
}
