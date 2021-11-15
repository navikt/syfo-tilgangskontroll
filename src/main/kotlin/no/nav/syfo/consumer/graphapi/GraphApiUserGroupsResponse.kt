package no.nav.syfo.consumer.graphapi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphApiGroup(
    val id: String,
    val displayName: String?,
    val mailNickname: String?,
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphApiUserGroupListResponse(
    val value: List<GraphApiGroup>,
) : Serializable
