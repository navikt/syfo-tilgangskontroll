import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer

group = "no.nav.syfo"
version = "1.0.0"

val apacheHttpClientVersion = "4.5.13"
val kotlinJacksonVersion = "2.13.2"
val logbackVersion = "7.1.1"
val micrometerVersion = "1.10.5"
val slf4jVersion = "1.7.36"
val tokenValidationSpringSupportVersion = "2.1.3"
val tokenValidationSpringTestSupportVersion = "2.0.0"
val logbackSyslog4jVersion = "1.0.0"
val snakeYamlVersion = "1.33"

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20"
    id("org.springframework.boot") version "2.7.10"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
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

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.yaml:snakeyaml:$snakeYamlVersion")

    implementation("no.nav.security:token-validation-spring:$tokenValidationSpringSupportVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    implementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")

    implementation("com.papertrailapp:logback-syslog4j:$logbackSyslog4jVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-test-support:$tokenValidationSpringTestSupportVersion")
}

kotlin {
    jvmToolchain(17)
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
        configureEach {
            append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
            append("META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports")
        }
        archiveBaseName.set("app")
        archiveClassifier.set("")
        archiveVersion.set("")
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }
}
