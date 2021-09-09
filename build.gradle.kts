import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val apacheHttpClientVersion = "4.5.13"
val javaxActivationVersion = "1.2.0"
val jaxRiVersion = "2.3.3"
val kotlinJacksonVersion = "2.11.3"
val logbackVersion = "6.3"
val prometheusVersion = "1.6.3"
val slf4jVersion = "1.7.30"
val tokenValidationSpringSupportVersion = "1.3.2"

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.21"
    id("org.springframework.boot") version "2.4.6"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("com.sun.xml.ws:jaxws-ri:$jaxRiVersion")
    implementation("com.sun.activation:javax.activation:$javaxActivationVersion")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("no.nav.security:token-validation-spring:$tokenValidationSpringSupportVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")

    implementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-test-support:$tokenValidationSpringSupportVersion")
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.ApplicationKt"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    test {
        useJUnitPlatform()
    }

    withType<ShadowJar> {
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
