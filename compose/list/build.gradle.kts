plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.icon)
                api(projects.core.model)
            }
        }
    }
}
