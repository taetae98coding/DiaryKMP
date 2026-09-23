plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                implementation(projects.compose.list)
                api(projects.core.model)
                implementation(projects.library.composeUi)
                api(libs.androidx.paging.compose)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
