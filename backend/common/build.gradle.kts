import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java")
    id("com.adarshr.test-logger") version "4.0.+"
    id("jacoco")
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

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

testlogger {
    theme = ThemeType.MOCHA_PARALLEL
    showSummary = true
}
