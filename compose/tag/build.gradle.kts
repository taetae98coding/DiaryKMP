plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                implementation(projects.compose.icon)
                implementation(projects.library.composeUi)
                api(libs.androidx.paging.compose)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
