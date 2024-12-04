import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java")
    id("com.adarshr.test-logger") version "4.0.+"
    id("jacoco")
    id("org.sonarqube") version "5.1.+"
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.knightsofdarkness"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("com.github.victools:jsonschema-generator:4.37.0")
    implementation("org.json:json:20240303")
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-ea")
    reports {
        junitXml.required.set(true)
    }
}

sonar {
  properties {
    property("sonar.projectKey", "Uprzejmy_KnightsOfDarkness")
    property("sonar.organization", "uprzejmy")
    property("sonar.host.url", "https://sonarcloud.io")
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
