package no.nav.syfo

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import kotlin.jvm.JvmStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableJwtTokenValidation
@SpringBootApplication
class LocalApplication

fun main(args: Array<String>) {
    runApplication<LocalApplication>(*args)
}
