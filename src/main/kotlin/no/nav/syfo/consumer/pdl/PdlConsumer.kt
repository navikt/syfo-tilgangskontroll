package no.nav.syfo.consumer.pdl

import io.micrometer.core.annotation.Timed
import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.ALLE_TEMA_HEADERVERDI
import no.nav.syfo.util.TEMA_HEADER
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*

@Service
class PdlConsumer(
    private val metric: Metric,
    private val azureAdTokenConsumer: AzureAdTokenConsumer,
    private val restTemplate: RestTemplate,
    @Value("\${pdl.url}") private val pdlUrl: String,
    @Value("\${pdl.client.id}") private val pdlClientId: String,
    private val cacheManager: CacheManager,
) {
    fun geografiskTilknytning(ident: String): GeografiskTilknytning {
        return geografiskTilknytningResponse(ident)?.geografiskTilknytning()
            ?: throw PdlRequestFailedException("No Geografisk Tilknytning was found in response from PDL")
    }

    fun geografiskTilknytningResponse(ident: String): PdlHentGeografiskTilknytning? {
        val cachedObject = getPDLGeografiskCache().get(ident)?.get() as PdlHentGeografiskTilknytning?
        return if (cachedObject != null) {
            cachedObject
        } else {
            val query = getPdlQuery("/pdl/hentGeografiskTilknytning.graphql")
            val request = PdlGeografiskTilknytningRequest(
                query = query,
                variables = PdlGeografiskTilknytningRequestVariables(ident)
            )
            val entity = HttpEntity(
                request,
                createRequestHeaders()
            )
            try {
                val pdlPerson = restTemplate.exchange(
                    pdlUrl,
                    HttpMethod.POST,
                    entity,
                    PdlGeografiskTilknytningResponse::class.java
                )
                val pdlPersonReponse = pdlPerson.body!!
                if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                    metric.countEvent(CALL_PDL_GT_FAIL)
                    pdlPersonReponse.errors.forEach {
                        LOG.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                    }
                    null
                } else {
                    metric.countEvent(CALL_PDL_GT_SUCCESS)
                    getPDLGeografiskCache().put(ident, pdlPersonReponse.data)
                    pdlPersonReponse.data
                }
            } catch (exception: RestClientResponseException) {
                metric.countEvent(CALL_PDL_GT_FAIL)
                LOG.error("Error from PDL with request-url: $pdlUrl", exception)
                throw exception
            }
        }
    }

    fun isKode6(pdlHentPerson: PdlHentPerson?): Boolean {
        return pdlHentPerson?.isKode6() ?: throw PdlRequestFailedException()
    }

    fun isKode7(pdlHentPerson: PdlHentPerson?): Boolean {
        return pdlHentPerson?.isKode7() ?: throw PdlRequestFailedException()
    }

    @Timed("syfotilgangskontroll_pdlConsumer_person", histogram = true)
    fun person(personIdentNumber: String): PdlHentPerson? {
        val cachedObject = getPDLPersonCache().get(personIdentNumber)?.get() as PdlHentPerson?
        return if (cachedObject != null) {
            cachedObject
        } else {
            val query = getPdlQuery("/pdl/hentPerson.graphql")
            val request = PdlPersonRequest(
                query = query,
                variables = PdlPersonRequestVariables(personIdentNumber)
            )
            val entity = HttpEntity(
                request,
                createRequestHeaders()
            )
            try {
                val pdlPerson = restTemplate.exchange(
                    pdlUrl,
                    HttpMethod.POST,
                    entity,
                    PdlPersonResponse::class.java
                )

                val pdlPersonReponse = pdlPerson.body!!
                if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                    metric.countEvent(CALL_PDL_PERSON_FAIL)
                    pdlPersonReponse.errors.forEach {
                        LOG.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                    }
                    null
                } else {
                    metric.countEvent(CALL_PDL_PERSON_SUCCESS)
                    getPDLPersonCache().put(personIdentNumber, pdlPersonReponse.data)
                    pdlPersonReponse.data
                }
            } catch (exception: RestClientException) {
                metric.countEvent(CALL_PDL_PERSON_FAIL)
                LOG.error("Error from PDL with request-url: $pdlUrl", exception)
                throw exception
            }
        }
    }

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    private fun createRequestHeaders(): HttpHeaders {
        val azureADSystemToken = azureAdTokenConsumer.getSystemToken(
            scopeClientId = pdlClientId,
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
        headers.setBearerAuth(azureADSystemToken)
        return headers
    }

    private fun getPDLGeografiskCache(): Cache =
        cacheManager.getCache(CacheConfig.CACHENAME_PDL_GEOGRAFISK_TILKNYTNING)!!

    private fun getPDLPersonCache(): Cache =
        cacheManager.getCache(CacheConfig.CACHENAME_PDL_PERSON)!!

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)

        private const val CALL_PDL_BASE = "call_pdl"
        const val CALL_PDL_GT_FAIL = "${CALL_PDL_BASE}_gt_fail"
        const val CALL_PDL_GT_SUCCESS = "${CALL_PDL_BASE}_gt_success"
        const val CALL_PDL_PERSON_FAIL = "${CALL_PDL_BASE}_fail"
        const val CALL_PDL_PERSON_SUCCESS = "${CALL_PDL_BASE}_success"
    }
}
