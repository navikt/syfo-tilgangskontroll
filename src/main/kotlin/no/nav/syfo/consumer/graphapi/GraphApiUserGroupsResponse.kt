package no.nav.syfo.consumer.ldap

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphApiGroup(
    val id: String,
    val displayName: String?,
    val mailNickname: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraphApiUserGroupListResponse(
    val value: List<GraphApiGroup>,
)
