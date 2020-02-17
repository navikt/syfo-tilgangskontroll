package no.nav.syfo.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;

@Controller
public class Metric {

    private final MeterRegistry registry;

    @Inject
    public Metric(MeterRegistry registry) {
        this.registry = registry;
    }

    public void countEvent(String name) {
        registry.counter(
                addPrefix(name),
                Tags.of("type", "info")
        ).increment();
    }

    public void tellHttpKall(int kode) {
        registry.counter(
                addPrefix("httpstatus"),
                Tags.of(
                        "type", "info",
                        "kode", String.valueOf(kode)
                )
        ).increment();
    }

    private String addPrefix(String navn) {
        String METRIKK_PREFIX = "syfotilgangskontroll_";
        return METRIKK_PREFIX + navn;
    }
}
