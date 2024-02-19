plugins {
    id("java")
    id("com.adarshr.test-logger") version "4.0.0"
}

group = "com.merynda"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.github.victools:jsonschema-generator:4.31.1")
    implementation("org.json:json:20231013")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}