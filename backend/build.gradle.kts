import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java")
    id("com.adarshr.test-logger") version "4.0.0"
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.knightsofdarkness"
version = project.findProperty("version")?.toString() ?: "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

// Keep patched maintenance releases ahead of the Spring Boot BOM defaults.
extra["jackson-bom.version"] = "3.1.8"
extra["jackson-2-bom.version"] = "2.21.8"
extra["tomcat.version"] = "11.0.27"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // the schema is the migrations under db/migration; Hibernate only checks that the entities match it
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework:spring-context")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    // hibernate stores battle and spy reports as JSON with its own Jackson mapper, which needs java.time support
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // the API described as OpenAPI, from which the browser and the simulation generate their types
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:3.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    // tests run on the same PostgreSQL as production, in a container
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    runtimeOnly("org.postgresql:postgresql:42.7.14")
    // simulations run many short throwaway worlds of the shipped jar, each in its own in-memory database
    runtimeOnly("com.h2database:h2")
    // bootRun starts the local PostgreSQL from compose.yaml; the jar never carries this
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-ea")
    reports {
        junitXml.required.set(true)
    }
}

testlogger {
    theme = ThemeType.MOCHA_PARALLEL
    showSummary = true
}

