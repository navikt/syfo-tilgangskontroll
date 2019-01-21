package no.nav.syfo;

import no.nav.syfo.ws.LogErrorHandler;
import no.nav.syfo.ws.WsOIDCClient;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.OrganisasjonRessursEnhetV1;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static java.util.Collections.singletonList;

@Configuration
@Profile("remote")
@SuppressWarnings("unchecked")
public class ApplicationConfig {

    /* Web service clients */

    @Bean
    @Primary
    public OrganisasjonEnhetV2 organisasjonEnhetV2(@Value("${virksomhet.organisasjonEnhet.v2.endpointurl}") String serviceUrl) {
        return new WsOIDCClient<OrganisasjonEnhetV2>().createPort(serviceUrl, OrganisasjonEnhetV2.class, singletonList(new LogErrorHandler()));
    }

    @Bean
    @Primary
    public EgenAnsattV1 egenAnsattV1(@Value("${virksomhet.egenansatt.v1.endpointurl}") String serviceUrl) {
        return new WsOIDCClient<EgenAnsattV1>().createPort(serviceUrl, EgenAnsattV1.class, singletonList(new LogErrorHandler()));
    }

    @Bean
    @Primary
    public OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1(@Value("${virksomhet.organisasjonressursenhet.v1.endpointurl}") String serviceUrl) {
        return new WsOIDCClient<OrganisasjonRessursEnhetV1>().createPort(serviceUrl, OrganisasjonRessursEnhetV1.class, singletonList(new LogErrorHandler()));
    }

    @Bean
    @Primary
    public PersonV3 personV3(@Value("${virksomhet.person.v3.endpointurl}") String serviceUrl) {
        return new WsOIDCClient<PersonV3>().createPort(serviceUrl, PersonV3.class, singletonList(new LogErrorHandler()));
    }

    @Bean
    public CacheManager RedisCacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisCacheConfiguration redisCacheConfig = RedisCacheConfiguration
                .defaultCacheConfig();
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(lettuceConnectionFactory)
                .cacheDefaults(redisCacheConfig)
                .build();
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory(new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel(new RedisNode("rfs-syfo-tilgangskontroll", 26379)));
    }

}
