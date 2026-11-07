plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.compose.map)
                api(projects.core.model)
                implementation(projects.library.composeUi)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
