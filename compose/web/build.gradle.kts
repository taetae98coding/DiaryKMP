plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                api(projects.core.model)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.webkit)
            }
        }
    }
}
