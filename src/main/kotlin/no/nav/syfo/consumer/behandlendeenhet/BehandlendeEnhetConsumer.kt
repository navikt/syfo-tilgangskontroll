package no.nav.syfo.consumer.behandlendeenhet

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class BehandlendeEnhetConsumer(
    private val azureAdTokenConsumer: AzureAdTokenConsumer,
    @Value("\${syfobehandlendeenhet.url}") private val baseUrl: String,
    @Value("\${syfobehandlendeenhet.client.id}") private val syfotilgangskontrollClientId: String,
    private val metric: Metric,
    @Qualifier("default") private val template: RestTemplate
) {
    private val behandlendeEnhetPersonIdentUrl = "$baseUrl$SYSTEM_V2_PERSONIDENT_PATH"

    @Cacheable(cacheNames = [CacheConfig.CACHENAME_BEHANDLENDEENHET_FNR], key = "#fnr", condition = "#fnr != null")
    fun getBehandlendeEnhet(fnr: String, callId: String?): BehandlendeEnhet {
        val oboToken = azureAdTokenConsumer.getSystemToken(
            scopeClientId = syfotilgangskontrollClientId,
        )
        try {
            val response = template.exchange(
                behandlendeEnhetPersonIdentUrl,
                HttpMethod.GET,
                createEntity(
                    callId = callId,
                    personIdentNumber = fnr,
                    token = oboToken,
                ),
                BehandlendeEnhet::class.java
            )
            val responseBody = response.body!!
            metric.countEvent(METRIC_CALL_BEHANDLENDEENHET_SUCCESS)
            return responseBody
        } catch (e: RestClientResponseException) {
            LOG.error("Error requesting BehandlendeEnhet from syfobehandlendeenhet with callId=$callId: ", e)
            metric.countEvent(METRIC_CALL_BEHANDLENDEENHET_FAIL)
            throw e
        }
    }

    private fun createEntity(
        callId: String?,
        personIdentNumber: String,
        token: String,
    ): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        headers[NAV_CALL_ID_HEADER] = getOrCreateCallId(callId)
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_PERSONIDENT_HEADER] = personIdentNumber
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BehandlendeEnhetConsumer::class.java)

        const val METRIC_CALL_BEHANDLENDEENHET_SUCCESS = "call_behandlendeenhet_success"
        const val METRIC_CALL_BEHANDLENDEENHET_FAIL = "call_behandlendeenhet_fail"

        const val SYSTEM_V2_PERSONIDENT_PATH = "/api/system/v2/personident"
    }
}
