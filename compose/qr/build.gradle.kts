plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                implementation(libs.qrose)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
