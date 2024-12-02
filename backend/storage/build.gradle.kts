plugins {
    java
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.+"
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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
    implementation(project(":common"))
    implementation(project(":game"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName("bootJar") {
    enabled = false
}
