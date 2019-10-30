package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
@EnableScheduling
@Profile({"remote"})
public class CacheEvictionScheduler {

    private final int MINUTE_MS = 60 * 1000;
    private final int HOUR_MS = 60 * MINUTE_MS;
    private final int CACHE_EVICTION_RATE = 12 * HOUR_MS;

    private CacheManager cachemanager;

    @Inject
    public CacheEvictionScheduler(CacheManager cachemanager) {
        this.cachemanager = cachemanager;
    }

    @Scheduled(fixedRate = CACHE_EVICTION_RATE)
    public void evictAllCachesAtInteval() {
        evictAllCaches();
    }

    private void evictAllCaches() {
        cachemanager.getCacheNames()
                .forEach(cacheName -> cachemanager.getCache(cacheName).clear());
    }
}
