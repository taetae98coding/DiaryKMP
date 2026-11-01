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

        androidMain {
            dependencies {
                implementation(projects.core.webCookie.api)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.webkit)
            }
        }
    }
}
