package no.nav.syfo;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableOIDCTokenValidation(ignore="org.springframework")
@EnableCaching
public class LocalApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalApplication.class, args);
    }
}

