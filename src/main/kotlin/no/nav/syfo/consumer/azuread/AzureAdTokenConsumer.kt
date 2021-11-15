package no.nav.syfo.consumer.azuread

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCUtil.getConsumerClientId
import no.nav.syfo.api.auth.getNAVIdentFromOBOToken
import no.nav.syfo.api.auth.getToken
import no.nav.syfo.cache.CacheConfig.Companion.CACHENAME_TOKENS
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.*
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
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
    @Value("\${graphapi.url}") private val graphApiUrl: String,
    private val cacheManager: CacheManager,
    private val contextHolder: TokenValidationContextHolder,
) {
    fun getOboToken(
        scopeClientId: String,
    ): String {
        val token = contextHolder.getToken()
        val veilederIdent = getNAVIdentFromOBOToken(contextHolder)
            ?: throw RuntimeException("Missing veilederId in OIDC-context")
        val azp = getConsumerClientId(contextHolder)

        val cacheKey = "$veilederIdent-$azp-$scopeClientId"
        val cachedToken = oboTokenCache().get(cacheKey)?.get() as AzureAdToken?
        return if (cachedToken?.isExpired() == false) {
            cachedToken.accessToken
        } else {
            try {
                val requestEntity = onBehalfOfRequestEntity(
                    scopeClientId = scopeClientId,
                    token = token,
                )
                val oboToken = getToken(requestEntity = requestEntity)
                oboTokenCache().put(cacheKey, oboToken)
                oboToken.accessToken
            } catch (e: RestClientResponseException) {
                log.error(
                    "Call to get AzureADV2Token from AzureAD for scope: $scopeClientId with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}",
                    e
                )
                throw e
            }
        }
    }

    fun getSystemToken(
        scopeClientId: String,
    ): String {
        val cachedToken = tokenCache[scopeClientId]
        return if (cachedToken == null || cachedToken.isExpired()) {
            try {
                val requestEntity = systemTokenRequestEntity(
                    scopeClientId = scopeClientId,
                )
                val token = getToken(requestEntity = requestEntity)
                tokenCache[scopeClientId] = token
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

    private fun onBehalfOfRequestEntity(
        scopeClientId: String,
        token: String
    ): HttpEntity<MultiValueMap<String, String>> {
        val scope = if (scopeClientId == graphApiUrl) {
            "$scopeClientId/.default"
        } else {
            "api://$scopeClientId/.default"
        }
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", azureAppClientId)
        body.add("client_secret", azureAppClientSecret)
        body.add("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("assertion", token)
        body.add("scope", scope)
        body.add("requested_token_use", "on_behalf_of")
        return HttpEntity<MultiValueMap<String, String>>(body, headers)
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

    private fun oboTokenCache(): Cache {
        return cacheManager.getCache(CACHENAME_TOKENS)!!
    }

    companion object {
        private val log = LoggerFactory.getLogger(AzureAdTokenConsumer::class.java)
        val tokenCache = ConcurrentHashMap<String, AzureAdToken>()
    }
}
