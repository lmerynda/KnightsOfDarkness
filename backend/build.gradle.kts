import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java")
    id("com.adarshr.test-logger") version "4.0.+"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.+"
    id("jacoco")
    id("org.sonarqube") version "5.1.+"
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.knightsofdarkness"
version = project.findProperty("version")?.toString() ?: "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-context")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.18.3")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.github.victools:jsonschema-generator:4.37.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("org.assertj:assertj-core:3.26.3")
    // runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql:42.7.5")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-ea")
    reports {
        junitXml.required.set(true)
    }
}

tasks.jacocoTestReport {
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
  }
}
