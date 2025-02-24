plugins {
    id("java")
    id("com.adarshr.test-logger") version "4.0.+"
    id("org.springframework.boot") version "3.4.1"
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
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-annotations")
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
    }
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-context")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(project(":common"))
    implementation(project(":game"))
    implementation(project(":storage"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    // runtimeOnly("org.postgresql:postgresql:42.7.5")
    runtimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active", "dev"))
}

tasks.named<JavaExec>("bootRun") {
    systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active", "dev"))
}
