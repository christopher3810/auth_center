pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// 빌드 스캔 플러그인 추가 (빌드 성능 분석 도구)
plugins {
    id("com.gradle.enterprise") version "3.14.1"
}

// 빌드 스캔 설정 (빌드 과정 분석 및 최적화에 도움)
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        // 빌드 실패 시에만 스캔 결과 발행
        publishOnFailure()
    }
}

// 빌드 캐시 설정 (반복 빌드 속도 향상)
buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30  // 30일 이상 사용하지 않은 캐시 항목 제거
    }
}

// 루트 프로젝트 이름 정의 - 이 설정은 필수입니다
rootProject.name = "auth_center" 