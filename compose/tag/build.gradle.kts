plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
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
