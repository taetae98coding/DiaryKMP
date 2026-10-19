plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.list)
                implementation(projects.domain.checklist)
                implementation(projects.feature.checklist.api)
                implementation(projects.feature.core)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.androidx.paging.testing)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
                implementation(projects.core.testing)
            }
        }
    }
}
