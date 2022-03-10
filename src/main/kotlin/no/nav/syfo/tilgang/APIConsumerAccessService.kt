package no.nav.syfo.tilgang

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.util.configuredJacksonMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class APIConsumerAccessService(
    @Value("\${AZURE_APP_PRE_AUTHORIZED_APPS}") private val azureAppPreAuthorizedApp: String,
) {
    private val preAuthorizedClientList: List<PreAuthorizedClient> = configuredJacksonMapper()
        .readValue(azureAppPreAuthorizedApp)

    fun getAuthorizedAppNameFromClientId(clientId: String): String =
        preAuthorizedClientList.find { it.clientId == clientId }?.toNamespaceAndApplicationName()?.applicationName ?: ""
}
