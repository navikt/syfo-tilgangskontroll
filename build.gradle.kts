import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val tokenValidationSpringSupportVersion = "1.3.0"
val springRetryVersion = "1.2.4.RELEASE"
val kotlinJacksonVersion = "2.10.0"
val logbackVersion = "6.3"

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.10"
    id("org.springframework.boot") version "2.2.0.RELEASE"
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

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("com.sun.xml.ws:jaxws-ri:2.3.2")
    implementation("com.sun.activation:javax.activation:1.2.0")

    implementation("org.springframework.retry:spring-retry:$springRetryVersion")

    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.glassfish.jersey.core:jersey-common:2.26")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackVersion")
    implementation("no.nav.security:token-validation-spring:$tokenValidationSpringSupportVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.0.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("no.nav.security:token-validation-test-support:$tokenValidationSpringSupportVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
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
