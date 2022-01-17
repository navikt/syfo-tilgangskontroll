package no.nav.syfo.util

import no.nav.syfo.LocalApplication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LocalApplication::class])
class AuditLoggerTest {

    @Test
    fun `compliant to cef format`() {
        val cef = CEF(suid = "X123456", duid = "01010199999", event = AuditLogEvent.Access, permit = true)

        Assertions.assertTrue(
           cef.toString()
                .startsWith(
                    "CEF:0|syfo-tilgangskontroll|auditLog|1.0|audit:access|syfo-tilgangskontroll audit log|INFO|"
                )
        )
        Assertions.assertTrue(cef.toString().endsWith("suid=X123456 duid=01010199999 flexString1Label=Decision flexString1=Permit"))
    }
}

