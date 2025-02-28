plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"

    kotlin("jvm") version "2.0.10"
    kotlin("plugin.spring") version "2.0.10"
    kotlin("plugin.jpa") version "2.0.10"

    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "com.deepromeet"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

noArg {
    annotation("com.deepromeet.atcha.common.annotation.NoArg")
}

extra["springCloudVersion"] = "2024.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // xml parsing
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.4")

    // openFeign
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // rest assured
    testImplementation("io.rest-assured:rest-assured:5.5.0")

    // H2
    runtimeOnly("com.h2database:h2")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

detekt {
    config.setFrom(
        files("$rootDir/config/detekt.yml")
    )
    autoCorrect = true
    buildUponDefaultConfig = true
    debug = true
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        sarif.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
    }

    jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("initSetting") {
    group = "custom tasks"
    description = "Execute both copyHooks and copySecret tasks."

    dependsOn("copyHooks", "copySecret")
}

tasks.register("copyHooks") {
    group = "git hooks"
    description = "Copy pre-commit and pre-push git hooks from .githooks to .git/hooks folder."

    doLast {
        // pre-commit hook 복사
        copy {
            from("$rootDir/.githooks/pre-commit")
            into("$rootDir/.git/hooks")
        }
        // pre-push hook 복사
        copy {
            from("$rootDir/.githooks/pre-push")
            into("$rootDir/.git/hooks")
        }
        // pre-push hook에 실행 권한 부여
        file("$rootDir/.git/hooks/pre-push").setExecutable(true)
        println("Git pre-commit 및 pre-push hook이 성공적으로 등록되었습니다.")
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("copySecret")
}

tasks.register<Copy>("copySecret") {
    from("./16th-team6-BE-submodule")
    include("secret-env.yml")
    into("src/main/resources")
}
