package no.nav.syfo.consumer.msgraph

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class TokenConsumer @Autowired constructor(private val restTemplate: RestTemplate) {
    companion object {
        private val clientId = System.getenv("AAD_CLIENT_ID")
        private val clientSecret = System.getenv("AAD_CLIENT_SECRET")
        private val tenantId = System.getenv("AAD_TENANT_ID")
        private val AzureOauthTokenEndpoint = "https://login.microsoftonline.com/$tenantId/oauth2/v2.0/token"
        private const val graphApiAccountNameQuery = "https://graph.microsoft.com/v1.0/me/?\$select=onPremisesSamAccountName"
    }

    fun getSubjectFromMsGraph(accessToken: String): String {
        return try {
            val oboToken = exchangeAccessTokenForOnBehalfOfToken(accessToken)
            callMsGraphApi(oboToken)
        } catch (e: Exception) {
            throw RuntimeException("Klarte ikke hente veileder-ident fra Azure AD access token: ${e.message}")
        }
    }

    private fun exchangeAccessTokenForOnBehalfOfToken(token: String): String {
        val response = restTemplate.exchange(
            AzureOauthTokenEndpoint,
            HttpMethod.POST,
            tokenEndpointEntity(token),
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

    private fun tokenEndpointEntity(token: String): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("client_id", clientId)
        body.add("client_secret", clientSecret)
        body.add("assertion", token)
        body.add("scope", "https://graph.microsoft.com/.default")
        body.add("requested_token_use", "on_behalf_of")
        return HttpEntity<MultiValueMap<String, String>>(body, headers)
    }

    private fun graphEntity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        return HttpEntity<String>(headers)
    }

}
