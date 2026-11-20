plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.jetbrains.compose.material3)
            }
        }
    }
}
