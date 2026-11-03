plugins {
    alias(libs.plugins.build.konfig) apply false
    alias(libs.plugins.dependency.guard) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.androidx.room3) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.performance) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt")
        // 외부에서 가져온 참고 코드는 원본 그대로 두어 다음 버전으로 갱신할 때 차이가 생기지 않게 한다.
        targetExclude("**/build/**", "DIARY_AGENTS/android-skills/**")
        ktlint()
        trimTrailingWhitespace()
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint()
        trimTrailingWhitespace()
    }
}
