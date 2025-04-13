import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.spring") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.1.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
}

group = "com.auth"
version = "0.0.1-SNAPSHOT"
java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://jitpack.io")
}

extra["springCloudVersion"] = "2024.0.1"

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JWT 관련 의존성
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // JPA 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // 암호화 관련 추가 의존성
    implementation("org.springframework.security:spring-security-crypto")

    // Web Client 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Common 라이브러리
    implementation("com.github.christopher3810:common_lib:v0.1.4")

    // Logger
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")

    // Swagger(OpenAPI) 설정
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test:3.7.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("org.springframework.security:spring-security-test")

    // MockK 관련 의존성
    testImplementation("io.mockk:mockk:1.13.17")

    // Kotest 관련 의존성
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("io.kotest:kotest-framework-datatest:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    // AOP 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Redis 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.20.6")
    testImplementation("org.testcontainers:junit-jupiter:1.20.6")
    testImplementation("org.testcontainers:postgresql:1.20.6")

    // 추후 ksp annotation processor 필요시
    // ksp("com.example:processor:1.0.0")
}

ktlint {
    version.set("1.5.0") // 1.4.1 아래 버전은 kotlin 2.0 이상 버전 이랑 충돌 이슈 있음.

    reporters {
        reporter(ReporterType.PLAIN) // 콘솔에 단순 텍스트 출력
    }

    additionalEditorconfig.set(
        mapOf(
            "max_line_length" to "150", // 최대 줄 길이를 150으로 설정
        ),
    )
    // 콘솔 출력을 활성화
    outputToConsole.set(true)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${extra["springCloudVersion"]}")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
    incremental = true // 증분 컴파일 활성화 - 변경된 파일만 컴파일
    outputs.cacheIf { true } // 컴파일 결과 캐싱으로 빌드 성능 향상
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1) // 병렬 테스트 실행
    setForkEvery(500) // 메모리 누수 방지를 위해 500개 테스트마다 새 JVM 프로세스 시작
    reports.html.required = false // HTML 리포트 활성화 여부
    reports.junitXml.required = true // XML 리포트 활성화 여부

    systemProperty("kotest.framework.classpath.scanning.autoscan", "true") // 자동 스캔으로 확장 감지
    systemProperty(
        "kotest.framework.parallelism",
        (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1).toString(),
    )
}

if (!project.hasProperty("prod")) {
    tasks.withType<Test> {
        filter {
            excludeTestsMatching("*IntegrationTest") // prod 는 통테 제외
        }
    }
}
