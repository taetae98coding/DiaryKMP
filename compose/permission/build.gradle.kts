plugins {
    alias(libs.plugins.convention.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.permission)
                implementation(libs.jetbrains.compose.ui)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
                implementation(libs.jetbrains.compose.material3)
            }
        }
    }
}
