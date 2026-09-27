plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.calendar)
                implementation(projects.compose.core)
                implementation(projects.library.kotlinxDatetime)
            }
        }
    }
}
