plugins {
    java
    id("org.springframework.boot") version "4.0.4"
    id("io.spring.dependency-management") version "1.1.7"
    checkstyle
}

group = "com.zimdugo"
version = "0.0.1-SNAPSHOT"
description = "zimdugo-be"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Web / Actuator
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Security / OAuth2 / JWT
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("org.locationtech.jts:jts-core")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // API Docs
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // AWS S3
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:sso")
    implementation("software.amazon.awssdk:ssooidc")

    // OCI Object Storage
    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage")
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey3")

    // Image metadata
    implementation("com.drewnoakes:metadata-extractor:2.19.0")

    // DB
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("com.h2database:h2")

    // Netty DNS (mac OS 관련입니다)
    runtimeOnly("io.netty:netty-resolver-dns-native-macos") {
        artifact {
            classifier = "osx-aarch_64"
        }
    }

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-data-jpa-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:2.32.4")
        mavenBom("com.oracle.oci.sdk:oci-java-sdk-bom:3.93.0")
    }
    dependencies {
        dependency("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")
        dependency("org.glassfish.jersey.connectors:jersey-apache-connector:3.0.8")
        dependency("org.glassfish.jersey.core:jersey-client:3.0.8")
        dependency("org.glassfish.jersey.core:jersey-common:3.0.8")
        dependency("org.glassfish.jersey.ext:jersey-entity-filtering:3.0.8")
        dependency("org.glassfish.jersey.inject:jersey-hk2:3.0.8")
        dependency("org.glassfish.jersey.media:jersey-media-json-jackson:3.0.8")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "13.2.0"
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    maxWarnings = 0
    maxErrors = 0
}

tasks.withType<Checkstyle> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

val gitHooksDirectory = providers.exec {
    commandLine("git", "rev-parse", "--git-path", "hooks")
}.standardOutput.asText.map { it.trim() }

tasks.register<Copy>("installGitHooks") {
    description = "pre-commit hook을 설치합니다 (Checkstyle 검사 자동 실행)"
    group = "setup"
    onlyIf {
        !file(gitHooksDirectory.get()).resolve("pre-commit").exists()
    }
    from("${rootDir}/scripts/pre-commit")
    into(gitHooksDirectory)
    filePermissions {
        unix("rwxr-xr-x")
    }
}

tasks.named("compileJava") {
    dependsOn("installGitHooks")
}
