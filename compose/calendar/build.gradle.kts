plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                implementation(projects.library.composeUi)
                implementation(projects.library.kotlinxDatetime)
            }
        }
    }
}
