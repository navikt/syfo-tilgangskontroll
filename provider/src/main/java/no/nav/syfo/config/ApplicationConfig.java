package no.nav.syfo.config;

import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.TimerAspect;
import no.nav.syfo.selftest.HealthCheckService;
import no.nav.syfo.selftest.IsAliveServlet;
import org.springframework.context.annotation.*;

@Configuration
@EnableAspectJAutoProxy
@Import({
        CacheConfig.class,
        ServiceConfig.class,
        DiskresjonskodeConfig.class,
        EgenAnsattConfig.class,
        LdapContext.class
})
@ComponentScan(basePackages = "no.nav.syfo.rest")
public class ApplicationConfig {

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public CountAspect countAspect() {
        return new CountAspect();
    }
    @Bean
    public IsAliveServlet isAliveServlet() {
        return new IsAliveServlet();
    }

    @Bean
    public HealthCheckService healthCheckService() {
        return new HealthCheckService();
    }

}
