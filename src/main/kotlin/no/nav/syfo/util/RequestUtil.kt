package no.nav.syfo.util

import java.util.*

const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
const val APP_CONSUMER_ID = "syfo-tilgangskontroll"
const val NAV_CALL_ID_HEADER = "Nav-Call-Id"

fun createCallId(): String = UUID.randomUUID().toString()

fun getOrCreateCallId(callId: String?): String = callId ?: UUID.randomUUID().toString()
