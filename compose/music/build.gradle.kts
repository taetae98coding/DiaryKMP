plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                implementation(projects.domain.playlist)
                implementation(libs.coil.compose)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
