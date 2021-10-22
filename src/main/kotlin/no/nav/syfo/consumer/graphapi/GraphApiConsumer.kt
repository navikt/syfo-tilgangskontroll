package no.nav.syfo.consumer.graphapi

import no.nav.syfo.consumer.azuread.AzureAdTokenConsumer
import no.nav.syfo.consumer.ldap.GraphApiGroup
import no.nav.syfo.consumer.ldap.GraphApiUserGroupListResponse
import no.nav.syfo.domain.AdRolle
import no.nav.syfo.metric.Metric
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class GraphApiConsumer(
    private val azureAdTokenConsumer: AzureAdTokenConsumer,
    @Value("\${graphapi.url}") val baseUrl: String,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
) {
    fun hasAccess(
        veilederIdent: String,
        adRolle: AdRolle,
    ): Boolean {
        try {
            val oboToken = azureAdTokenConsumer.getOboToken(
                scopeClientId = baseUrl,
            )

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.setBearerAuth(oboToken)

            val url = "$baseUrl/v1.0/$GRAPHAPI_USER_GROUPS_PATH"
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<String>(headers),
                GraphApiUserGroupListResponse::class.java,
            )
            val groupList = response.body!!
            metric.countEvent(CALL_GRAPHAPI_USER_GROUPS_SUCCESS)

            return isRoleInUserGroupList(
                groupList = groupList.value,
                adRolle = adRolle,
            )
        } catch (e: RestClientResponseException) {
            log.error(
                "Call to get response from Microsoft Graph failed with status: {} and message: {}.",
                e.rawStatusCode,
                e.responseBodyAsString
            )
            metric.countEvent(CALL_GRAPHAPI_USER_GROUPS_FAIL)
            throw e
        }
    }

    private fun isRoleInUserGroupList(
        groupList: List<GraphApiGroup>,
        adRolle: AdRolle,
    ): Boolean {
        return groupList.map { it.id }.contains(adRolle.id)
    }

    companion object {
        const val GRAPHAPI_USER_GROUPS_PATH = "/me/memberOf"

        private const val CALL_GRAPHAPI_USER_GROUPS_BASE = "call_graphapi_user_groups"
        private const val CALL_GRAPHAPI_USER_GROUPS_FAIL = "${CALL_GRAPHAPI_USER_GROUPS_BASE}_fail"
        private const val CALL_GRAPHAPI_USER_GROUPS_SUCCESS = "${CALL_GRAPHAPI_USER_GROUPS_BASE}_success"

        private val log: Logger = LoggerFactory.getLogger(GraphApiConsumer::class.java)
    }
}
