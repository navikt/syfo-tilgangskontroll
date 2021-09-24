package no.nav.syfo.consumer.azuread

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.*
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ConcurrentHashMap

@Component
class AzureAdTokenConsumer @Autowired constructor(
    @Qualifier("restTemplateProxy") private val restTemplateProxy: RestTemplate,
    @Value("\${azure.app.client.id}") private val azureAppClientId: String,
    @Value("\${azure.app.client.secret}") private val azureAppClientSecret: String,
    @Value("\${azure.openid.config.token.endpoint}") private val azureTokenEndpoint: String,
) {
    fun getSystemToken(
        scopeClientId: String,
    ): String {
        val cachedToken = systemTokenCache[scopeClientId]
        return if (cachedToken == null || cachedToken.isExpired()) {
            try {
                val requestEntity = systemTokenRequestEntity(
                    scopeClientId = scopeClientId,
                )
                val token = getToken(requestEntity = requestEntity)
                systemTokenCache[scopeClientId] = token
                token.accessToken
            } catch (e: RestClientResponseException) {
                log.error(
                    "Call to get AzureADV2Token from AzureAD as system for scope: $scopeClientId with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}",
                    e
                )
                throw e
            }
        } else {
            cachedToken.accessToken
        }
    }

    fun getToken(
        requestEntity: HttpEntity<MultiValueMap<String, String>>,
    ): AzureAdToken {
        val response = restTemplateProxy.exchange(
            azureTokenEndpoint,
            HttpMethod.POST,
            requestEntity,
            AzureAdV2TokenResponse::class.java
        )
        val tokenResponse = response.body!!

        return tokenResponse.toAzureAdV2Token()
    }

    fun systemTokenRequestEntity(
        scopeClientId: String,
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", azureAppClientId)
        body.add("scope", "api://$scopeClientId/.default")
        body.add("grant_type", "client_credentials")
        body.add("client_secret", azureAppClientSecret)

        return HttpEntity<MultiValueMap<String, String>>(body, headers)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AzureAdTokenConsumer::class.java)
        val systemTokenCache = ConcurrentHashMap<String, AzureAdToken>()
    }
}
