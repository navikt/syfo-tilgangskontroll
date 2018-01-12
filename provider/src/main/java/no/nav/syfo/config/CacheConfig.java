package no.nav.syfo.config;

import no.nav.sbl.dialogarena.common.cxf.userkeygenerator.UserKeyGenerator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public EhCacheCacheManager cacheManager() {
        return new EhCacheCacheManager();
    }

    @Bean
    public UserKeyGenerator userkeygenerator() {
        return new UserKeyGenerator();
    }

    @Bean
    public SimpleKeyGenerator simplekeygenerator() {
        return new SimpleKeyGenerator();
    }

}
