plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
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
