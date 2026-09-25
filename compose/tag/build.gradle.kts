plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                api(projects.core.model)
                implementation(projects.library.composeUi)
                api(libs.androidx.paging.compose)
            }
        }

        androidHostTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
