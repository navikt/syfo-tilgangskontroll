package no.nav.syfo;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
@EnableOIDCTokenValidation
public class Application {
//    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurerDefault() {
//        PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
//        properties.setLocation(new FileSystemResource("./application.yaml"));
//        properties.setIgnoreResourceNotFound(false);
//        return properties;
//    }
//
//    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurerAdditional() {
//        PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
//        properties.setLocation(new FileSystemResource("./config.properties"));
//        properties.setIgnoreResourceNotFound(false);
//        return properties;
//    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
