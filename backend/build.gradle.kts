import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java")
    id("com.adarshr.test-logger") version "4.0.0"
    id("org.springframework.boot") version "4.0.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
    id("org.sonarqube") version "7.5.0.8588"
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.knightsofdarkness"
version = project.findProperty("version")?.toString() ?: "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    implementation("org.springframework:spring-context")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.github.victools:jsonschema-generator:4.38.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    // runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql:42.7.14")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-ea")
    reports {
        junitXml.required.set(true)
    }
}

// Preserve coverage diagnostics when an assertion fails.
tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    mustRunAfter(tasks.test)
    reports {
        xml.required.set(true)
    }
}

testlogger {
    theme = ThemeType.MOCHA_PARALLEL
    showSummary = true
}

sonar {
  properties {
    property("sonar.projectKey", "Uprzejmy_KnightsOfDarkness")
    property("sonar.organization", "uprzejmy")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
  }
}
