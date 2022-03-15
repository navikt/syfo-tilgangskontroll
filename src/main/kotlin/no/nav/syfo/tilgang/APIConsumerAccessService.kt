package no.nav.syfo.tilgang

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCUtil.getConsumerClientId
import no.nav.syfo.util.configuredJacksonMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class APIConsumerAccessService(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    @Value("\${azure.app.pre.authorized.apps}") private val azureAppPreAuthorizedApps: String,
) {
    private val preAuthorizedClientList: List<PreAuthorizedClient> = configuredJacksonMapper()
        .readValue(azureAppPreAuthorizedApps)

    fun getAuthorizedAppNameFromClientId(clientId: String): String =
        preAuthorizedClientList.find { it.clientId == clientId }?.toNamespaceAndApplicationName()?.applicationName ?: ""

    fun isConsumerApplicationAZPAuthorized(authorizedApplicationNameList: List<String>): Boolean {
        val clientIdList = preAuthorizedClientList
            .filter { authorizedApplicationNameList.contains(it.toNamespaceAndApplicationName().applicationName) }
            .map { it.clientId }

        val consumerClientIdAzp = getConsumerClientId(contextHolder = tokenValidationContextHolder)
        return clientIdList.contains(consumerClientIdAzp)
    }
}
