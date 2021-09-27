package no.nav.syfo.consumer.pdl

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.ALLE_TEMA_HEADERVERDI
import no.nav.syfo.util.TEMA_HEADER
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*

@Service
class PdlConsumer(
    private val metric: Metric,
    private val azureAdTokenConsumer: AzureAdTokenConsumer,
    @Qualifier("restTemplateProxy") private val restTemplateProxy: RestTemplate,
    @Value("\${pdl.url}") private val pdlUrl: String,
    @Value("\${pdl.client.id}") private val pdlClientId: String,
) {
    fun geografiskTilknytning(ident: String): GeografiskTilknytning {
        return geografiskTilknytningResponse(ident)?.geografiskTilknytning()
            ?: throw PdlRequestFailedException("No Geografisk Tilknytning was found in response from PDL")
    }

    @Cacheable(cacheNames = [CacheConfig.CACHENAME_PDL_GEOGRAFISK_TILKNYTNING], key = "#ident", condition = "#ident != null")
    fun geografiskTilknytningResponse(ident: String): PdlHentGeografiskTilknytning? {
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
            val pdlPerson = restTemplateProxy.exchange(
                pdlUrl,
                HttpMethod.POST,
                entity,
                PdlGeografiskTilknytningResponse::class.java
            )
            val pdlPersonReponse = pdlPerson.body!!
            return if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                metric.countEvent(CALL_PDL_GT_FAIL)
                pdlPersonReponse.errors.forEach {
                    LOG.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                metric.countEvent(CALL_PDL_GT_SUCCESS)
                pdlPersonReponse.data
            }
        } catch (exception: RestClientResponseException) {
            metric.countEvent(CALL_PDL_GT_FAIL)
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
        }
    }

    fun isKode6(pdlHentPerson: PdlHentPerson?): Boolean {
        return pdlHentPerson?.isKode6() ?: throw PdlRequestFailedException()
    }

    fun isKode7(pdlHentPerson: PdlHentPerson?): Boolean {
        return pdlHentPerson?.isKode7() ?: throw PdlRequestFailedException()
    }

    @Cacheable(cacheNames = [CacheConfig.CACHENAME_PDL_PERSON], key = "#personIdentNumber", condition = "#personIdentNumber != null")
    fun person(personIdentNumber: String): PdlHentPerson? {
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
            val pdlPerson = restTemplateProxy.exchange(
                pdlUrl,
                HttpMethod.POST,
                entity,
                PdlPersonResponse::class.java
            )

            val pdlPersonReponse = pdlPerson.body!!
            return if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                metric.countEvent(CALL_PDL_PERSON_FAIL)
                pdlPersonReponse.errors.forEach {
                    LOG.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                metric.countEvent(CALL_PDL_PERSON_SUCCESS)
                pdlPersonReponse.data
            }
        } catch (exception: RestClientException) {
            metric.countEvent(CALL_PDL_PERSON_FAIL)
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
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

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)

        private const val CALL_PDL_BASE = "call_pdl"
        const val CALL_PDL_GT_FAIL = "${CALL_PDL_BASE}_gt_fail"
        const val CALL_PDL_GT_SUCCESS = "${CALL_PDL_BASE}_gt_success"
        const val CALL_PDL_PERSON_FAIL = "${CALL_PDL_BASE}_fail"
        const val CALL_PDL_PERSON_SUCCESS = "${CALL_PDL_BASE}_success"
    }
}
