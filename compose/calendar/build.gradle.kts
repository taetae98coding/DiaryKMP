plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.composeUi)
                implementation(projects.library.kotlinxDatetime)
            }
        }
    }
}
