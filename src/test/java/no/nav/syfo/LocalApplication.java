package no.nav.syfo;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJwtTokenValidation(ignore = "org.springframework")
public class LocalApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalApplication.class, args);
    }
}

