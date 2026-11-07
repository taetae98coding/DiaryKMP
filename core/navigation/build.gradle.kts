plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.androidx.navigation3.runtime)
            }
        }
    }
}
