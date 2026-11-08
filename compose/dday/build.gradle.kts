plugins {
    alias(libs.plugins.convention.compose.component)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                implementation(projects.domain.dday)
                implementation(projects.library.composeUi)
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
