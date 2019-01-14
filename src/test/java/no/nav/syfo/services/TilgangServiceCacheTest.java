package no.nav.syfo.services;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.domain.Tilgang;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

import static java.lang.Boolean.FALSE;
import static no.nav.syfo.domain.AdRoller.SYFO;
import static no.nav.syfo.services.TilgangService.TILGANGTILBRUKER;
import static no.nav.syfo.services.TilgangService.TILGANGTILTJENESTEN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


/**
 * Component tests testing Spring's Cache Abstraction using Spring Data Redis auto-configured with Spring Boot.
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = LocalApplication.class,
        properties = {
                "spring.cache.type=redis",
                "spring.redis.host=localhost"
        }
)
public class TilgangServiceCacheTest {

    private static final String VEILEDER_1 = "Z000001";
    private static final String VEILEDER_2 = "Z000002";
    private static final String BRUKER_1 = "12345678910";
    private static final String BRUKER_2 = "12345678911";

    @Autowired
    private TilgangService tilgangService;

    @Autowired
    private LdapService ldapService;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private RedisCacheManager redisCacheManager;

    private static RedisServer redisServer;

    @BeforeClass
    public static void startRedis() {
        redisServer = new RedisServer();
        redisServer.start();
    }

    @Before
    public void reset(){
        Mockito.clearInvocations(ldapService);
        for (String cachename : redisCacheManager.getCacheNames()){
            redisCacheManager.getCache(cachename).clear();
        }
    }

    @AfterClass
    public static void stopRedis() {
        redisServer.stop();
    }

    @Test
    public void cacheTilgangTilTjenesten() {
        for (int i = 0; i < 10; i++) {
            tilgangService.harTilgangTilTjenesten(VEILEDER_1);
            tilgangService.harTilgangTilTjenesten(VEILEDER_2);
        }

        Cache cache = redisCacheManager.getCache(TILGANGTILTJENESTEN);
        assertEquals(FALSE, cache.get(VEILEDER_1).get());
        assertEquals(FALSE, cache.get(VEILEDER_2).get());

        verify(ldapService, times(1)).harTilgang(VEILEDER_1, SYFO.rolle);
        verify(ldapService, times(1)).harTilgang(VEILEDER_2, SYFO.rolle);
        verifyNoMoreInteractions(ldapService);
    }

    @Test
    public void cacheTilgangTilBrukeren() {

        for (int i = 0; i < 10; i++) {
            tilgangService.sjekkTilgang(BRUKER_1, VEILEDER_1);
            tilgangService.sjekkTilgang(BRUKER_2, VEILEDER_1);
            tilgangService.sjekkTilgang(BRUKER_1, VEILEDER_2);
            tilgangService.sjekkTilgang(BRUKER_2, VEILEDER_2);
        }

        final Tilgang tilgangNektetSyfo = new Tilgang().withHarTilgang(false).withBegrunnelse(SYFO.name());

        Cache cache = redisCacheManager.getCache(TILGANGTILBRUKER);
        assertEquals(tilgangNektetSyfo, cache.get(VEILEDER_1.concat(BRUKER_1)).get());
        assertEquals(tilgangNektetSyfo, cache.get(VEILEDER_1.concat(BRUKER_2)).get());
        assertEquals(tilgangNektetSyfo, cache.get(VEILEDER_2.concat(BRUKER_1)).get());
        assertEquals(tilgangNektetSyfo, cache.get(VEILEDER_2.concat(BRUKER_2)).get());

        // Vi forventer 2 kall ldapservice fordi den kalles en gang for hver ny bruker.
        // Men gjentatte kall med samme veileder og bruker blir cachet.
        verify(ldapService, times(2)).harTilgang(VEILEDER_1, SYFO.rolle);
        verify(ldapService, times(2)).harTilgang(VEILEDER_2, SYFO.rolle);
        verifyNoMoreInteractions(ldapService);
    }




}

