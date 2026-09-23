plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                api(projects.compose.map)
                api(projects.core.model)
                implementation(projects.library.composeUi)
            }
        }
    }
}
