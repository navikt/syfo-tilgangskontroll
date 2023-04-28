package no.nav.syfo.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class Metric @Inject constructor(
    private val registry: MeterRegistry,
) {
    fun countEvent(name: String) {
        registry.counter(
            addPrefix(name),
            Tags.of("type", "info")
        ).increment()
    }

    fun tellHttpKall(kode: Int) {
        registry.counter(
            addPrefix("httpstatus"),
            Tags.of(
                "type",
                "info",
                "kode",
                kode.toString()
            )
        ).increment()
    }

    private fun addPrefix(navn: String): String {
        val metricPrefix = "syfotilgangskontroll_"
        return metricPrefix + navn
    }
}
