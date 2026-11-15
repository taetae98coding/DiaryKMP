plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                implementation(projects.domain.routine)
                implementation(projects.library.composeUi)
                api(libs.androidx.paging.compose)
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
