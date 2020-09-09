import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val cxfVersion = "3.3.4"
val tokenValidationSpringSupportVersion = "1.3.0"
val egenAnsattV1Version = "1.2019.09.25-00.21-49b69f0625e0"
val organisasjonenhetV2Version = "1.2019.09.25-00.21-49b69f0625e0"
val personV3Version = "1.2019.07.11-06.47-b55f47790a9d"
val springBootVersion = "2.2.0.RELEASE"
val springRetryVersion = "1.2.4.RELEASE"
val kotlinLibVersion = "1.3.50"
val kotlinJacksonVersion = "2.10.0"
val logbackVersion = "6.3"

plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.0")
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("com.sun.xml.ws:jaxws-tools:2.3.1") {
            exclude(group = "com.sun.xml.ws", module = "policy")
        }
    }
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "http://packages.confluent.io/maven/")
    maven(url = "https://repo1.maven.org/maven2/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinLibVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinLibVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-logging:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-aop:$springBootVersion")

    implementation("com.sun.xml.ws:jaxws-ri:2.3.2")
    implementation("com.sun.activation:javax.activation:1.2.0")

    implementation("org.springframework.retry:spring-retry:$springRetryVersion")

    implementation("org.projectlombok:lombok:1.16.22")
    annotationProcessor("org.projectlombok:lombok:1.18.6")

    implementation("javax.ws.rs:javax.ws.rs-api:2.0.1")
    implementation("org.glassfish.jersey.core:jersey-common:2.26")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackVersion")
    implementation("no.nav.security:token-validation-spring:$tokenValidationSpringSupportVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.0.6")

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-core:$cxfVersion")

    implementation("no.nav.tjenestespesifikasjoner:egenansatt-v1-tjenestespesifikasjon:$egenAnsattV1Version")
    implementation("no.nav.tjenestespesifikasjoner:organisasjonenhet-v2-tjenestespesifikasjon:$organisasjonenhetV2Version")
    implementation("no.nav.tjenestespesifikasjoner:person-v3-tjenestespesifikasjon:$personV3Version")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("no.nav.security:token-validation-test-support:$tokenValidationSpringSupportVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.Application"
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

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "11"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "11"
    }
}
