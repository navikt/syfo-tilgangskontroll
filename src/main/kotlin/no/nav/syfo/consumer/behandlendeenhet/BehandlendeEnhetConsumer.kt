package no.nav.syfo.consumer.behandlendeenhet

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.metric.Metric
import no.nav.syfo.consumer.sts.StsConsumer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class BehandlendeEnhetConsumer(
    @Value("\${syfobehandlendeenhet.url}") private val baseUrl: String,
    private val metric: Metric,
    private val stsConsumer: StsConsumer,
    private val template: RestTemplate
) {

    @Cacheable(cacheNames = [CacheConfig.CACHENAME_BEHANDLENDEENHET_FNR], key = "#fnr", condition = "#fnr != null")
    fun getBehandlendeEnhet(fnr: String, callId: String?): BehandlendeEnhet {
        val stsToken = stsConsumer.token()

        val httpEntity = entity(callId, stsToken)
        try {
            val response = template.exchange<BehandlendeEnhet>(
                getBehandlendeEnhetUrl(fnr),
                HttpMethod.GET,
                httpEntity,
                BehandlendeEnhet::class.java
            )
            val responseBody = response.body!!
            metric.countEvent(METRIC_CALL_BEHANDLENDEENHET_SUCCESS)
            return responseBody
        } catch (e: RestClientResponseException) {
            LOG.error("Error requesting BehandlendeEnhet from syfobehandlendeenhet with callId ${httpEntity.headers[NAV_CALL_ID_HEADER]}: ", e)
            metric.countEvent(METRIC_CALL_BEHANDLENDEENHET_FAIL)
            throw e
        }
    }

    private fun getBehandlendeEnhetUrl(bruker: String) = "$baseUrl/api/$bruker"

    private fun entity(callId: String?, token: String): HttpEntity<String> {
        val credentials = bearerCredentials(token)
        val headers = HttpHeaders()
        headers[HttpHeaders.AUTHORIZATION] = credentials
        headers[NAV_CALL_ID_HEADER] = getOrCreateCallId(callId)
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BehandlendeEnhetConsumer::class.java)

        const val METRIC_CALL_BEHANDLENDEENHET_SUCCESS = "call_behandlendeenhet_success"
        const val METRIC_CALL_BEHANDLENDEENHET_FAIL = "call_behandlendeenhet_fail"
    }
}
