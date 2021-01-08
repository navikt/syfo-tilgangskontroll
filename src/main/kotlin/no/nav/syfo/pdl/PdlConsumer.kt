package no.nav.syfo.pdl

import no.nav.syfo.metric.Metric
import no.nav.syfo.sts.StsConsumer
import no.nav.syfo.util.ALLE_TEMA_HEADERVERDI
import no.nav.syfo.util.NAV_CONSUMER_TOKEN_HEADER
import no.nav.syfo.util.TEMA_HEADER
import no.nav.syfo.util.bearerCredentials
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class PdlConsumer(
    private val metric: Metric,
    @Value("\${pdl.url}") private val pdlUrl: String,
    private val stsConsumer: StsConsumer,
    private val restTemplate: RestTemplate
) {
    fun geografiskTilknytning(ident: String): String {
        return geografiskTilknytningResponse(ident)?.geografiskTilknytning()
            ?: throw PdlRequestFailedException("No Geografisk Tilknytning was found in response from PDL")
    }

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
            val pdlPerson = restTemplate.exchange(
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

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    private fun createRequestHeaders(): HttpHeaders {
        val stsToken: String = stsConsumer.token()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
        headers.set(AUTHORIZATION, bearerCredentials(stsToken))
        headers.set(NAV_CONSUMER_TOKEN_HEADER, bearerCredentials(stsToken))
        return headers
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)

        private const val CALL_PDL_BASE = "call_pdl"
        const val CALL_PDL_GT_FAIL = "${CALL_PDL_BASE}_gt_fail"
        const val CALL_PDL_GT_SUCCESS = "${CALL_PDL_BASE}_gt_success"
    }
}
