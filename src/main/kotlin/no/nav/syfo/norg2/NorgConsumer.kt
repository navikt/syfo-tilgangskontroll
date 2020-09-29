package no.nav.syfo.norg2

import no.nav.syfo.config.CacheConfig
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.NAV_CALL_ID_HEADER
import no.nav.syfo.util.createCallId
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Service
class NorgConsumer @Inject
constructor(
    @Value("\${norg2.url}") private val norg2BaseUrl: String,
    private val metric: Metric,
    private val restTemplate: RestTemplate
) {
    @Cacheable(cacheNames = [CacheConfig.CACHENAME_GEOGRAFISK_TILHORIGHET_ENHET], key = "#geografiskTilknytning", condition = "#geografiskTilknytning != null")
    fun getNAVKontorForGT(geografiskTilknytning: String): String {
        try {
            val result = restTemplate.exchange(
                getNAVKontorForGTUrl(geografiskTilknytning),
                HttpMethod.GET,
                entity(),
                NorgEnhet::class.java
            )
            val enhet = result.body!!
            metric.countEvent("call_norg2_getnavkontorforgt_success")
            return enhet.enhetNr
        } catch (e: RestClientResponseException) {
            metric.countEvent("call_norg2_getnavkontorforgt_fail")
            log.error("Call to NORG2-NAVkontorForGT failed with status HTTP-${e.rawStatusCode} for GeografiskTilknytning $geografiskTilknytning")
            throw e
        }
    }

    private fun getNAVKontorForGTUrl(geografiskTilknytning: String): String {
        return "$norg2BaseUrl/api/v1/enhet/navkontor/$geografiskTilknytning"
    }

    @Cacheable(cacheNames = [CacheConfig.CACHENAME_ENHET_OVERORDNET_ENHETER], key = "#enhetNr", condition = "#enhetNr != null")
    fun getOverordnetEnhetListForNAVKontor(enhetNr: String): List<String> {
        try {
            val result = restTemplate.exchange(
                getOverordnetEnhetForNAVKontorUrl(enhetNr),
                HttpMethod.GET,
                entity(),
                object : ParameterizedTypeReference<List<NorgEnhet>>() {}
            )
            val enhetList = result.body!!
            metric.countEvent("call_norg2_getoverordnetenhetforenhet_success")
            return enhetList.map {
                it.enhetNr
            }
        } catch (e: RestClientResponseException) {
            val message = "Call to NORG2-OverordnetEnhetForNAVKontor failed with status HTTP-${e.rawStatusCode} for enhetNr $enhetNr"
            if (e.rawStatusCode == 404) {
                log.warn(message)
                metric.countEvent("call_norg2_getoverordnetenhetforenhet_notfound")
                return emptyList()
            } else {
                log.error(message)
                metric.countEvent("call_norg2_getoverordnetenhetforenhet_fail")
                throw e
            }
        }
    }

    private fun getOverordnetEnhetForNAVKontorUrl(enhetNr: String): String {
        return "$norg2BaseUrl/api/v1/enhet/$enhetNr/overordnet?organiseringsType=$organiseringsType"
    }

    private fun entity(): HttpEntity<String>? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity(headers)
    }

    companion object {
        private val log = getLogger(NorgConsumer::class.java)

        private const val organiseringsType = "FYLKE"
    }
}
