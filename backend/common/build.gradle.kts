plugins {
	java
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
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
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.getByName("bootJar") {
    enabled = false
}