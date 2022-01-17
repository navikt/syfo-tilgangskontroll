package no.nav.syfo.util

import org.slf4j.LoggerFactory
import java.time.Instant

enum class AuditLogEvent {
    Create,
    Access,
    Update;

    override fun toString(): String {
        return if (this == Create) {
            "audit:create"
        } else if (this == Access) {
            "audit:access"
        } else {
            "audit:update"
        }
    }
}

data class CEF(val suid: String, val duid: String, val event: AuditLogEvent, val permit: Boolean) {
    override fun toString() =
        "CEF:0|syfo-tilgangskontroll|auditLog|1.0|$event|syfo-tilgangskontroll audit log|INFO|end=${
            Instant.now().toEpochMilli()
        } suid=$suid duid=$duid flexString1Label=Decision flexString1=${if (permit) "Permit" else "Deny"}"
}

private val auditLogger = LoggerFactory.getLogger("auditLogger");

fun auditLog(format: CEF) {
    auditLogger.info("$format")
}


