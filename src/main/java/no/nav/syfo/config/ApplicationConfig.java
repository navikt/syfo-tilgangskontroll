package no.nav.syfo.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.TimerAspect;
import no.nav.syfo.config.caching.CacheConfig;
import org.springframework.context.annotation.*;

@Configuration
@EnableAspectJAutoProxy
@Import({
        CacheConfig.class,
        ServiceConfig.class,
        DiskresjonskodeConfig.class,
        OrganisasjonRessursEnhetConfig.class,
        OrganisasjonEnhetConfig.class,
        PersonConfig.class,
        EgenAnsattConfig.class,
        LdapContext.class
})
@ComponentScan(basePackages = "no.nav.syfo.rest")
public class ApplicationConfig implements ApiApplication.NaisApiApplication {

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public CountAspect countAspect() {
        return new CountAspect();
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .issoLogin()
                .sts();
    }

    @Override
    public String getApplicationName() {
        return "syfo-tilgangskontroll";
    }

    @Override
    public Sone getSone() {
        return Sone.FSS;
    }
}