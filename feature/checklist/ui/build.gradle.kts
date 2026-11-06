plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.checklist)
                implementation(projects.compose.list)
                implementation(projects.compose.tag)
                implementation(projects.domain.checklist)
                implementation(projects.domain.tag)
                implementation(projects.feature.checklist.api)
                implementation(projects.feature.core)
                implementation(projects.feature.tag.api)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            }
        }
    }
}
