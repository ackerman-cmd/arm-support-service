import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

group = "com.base"
version = "0.0.1-SNAPSHOT"
description = "arm-support-service"

val ktlintVersion = "1.5.0"
val javaVersion = 21

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.16")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

ktlint {
    version.set(ktlintVersion)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(ReporterType.PLAIN)
    }
    filter {
        exclude("**/generated/**")
        include("**/*.kt")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    runImage = "paketobuildpacks/ubuntu-noble-run:latest"
}
