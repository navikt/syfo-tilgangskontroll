package no.nav.syfo.consumer.azuread

import org.springframework.beans.factory.annotation.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class TokenConsumer @Autowired constructor(
    @Qualifier("restTemplateWithProxy") private val restTemplateWithProxy: RestTemplate,
    private val restTemplate: RestTemplate,
    @Value("\${azure.app.client.id}") private val azureAppClientId: String,
    @Value("\${azure.app.client.secret}") private val azureAppClientSecret: String,
    @Value("\${azure.openid.config.token.endpoint}") private val azureTokenEndpoint: String,
) {
    fun getSubjectFromMsGraph(token: String): String {
        return try {
            val oboToken = getOnBehalfToken(
                scopeClientId = msGraphApiClientId,
                token = token,
            )
            callMsGraphApi(oboToken)
        } catch (e: Exception) {
            throw RuntimeException("Klarte ikke hente veileder-ident fra Azure AD access token: ${e.message}")
        }
    }

    private fun getOnBehalfToken(
        scopeClientId: String,
        token: String,
    ): String {
        val response = restTemplateWithProxy.exchange(
            azureTokenEndpoint,
            HttpMethod.POST,
            onBehalfOfRequestEntity(
                scopeClientId = scopeClientId,
                token = token,
            ),
            TokenResponse::class.java
        )
        return response.body!!.access_token
    }

    private fun callMsGraphApi(token: String): String {
        val response = restTemplate.exchange(
            graphApiAccountNameQuery,
            HttpMethod.GET,
            graphEntity(token),
            GraphResponse::class.java
        )
        return response.body!!.onPremisesSamAccountName
    }

    private fun onBehalfOfRequestEntity(
        scopeClientId: String,
        token: String,
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", azureAppClientId)
        body.add("client_secret", azureAppClientSecret)
        body.add("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("assertion", token)
        body.add("requested_token_use", "on_behalf_of")
        if (scopeClientId == msGraphApiClientId) {
            body.add("scope", "$scopeClientId/.default")
        } else {
            body.add("scope", "api://$scopeClientId/.default")
        }
        return HttpEntity<MultiValueMap<String, String>>(body, headers)
    }

    private fun graphEntity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return HttpEntity<String>(headers)
    }

    companion object {
        private const val msGraphApiClientId = "https://graph.microsoft.com"
        private const val graphApiAccountNameQuery = "https://graph.microsoft.com/v1.0/me/?\$select=onPremisesSamAccountName"
    }
}
