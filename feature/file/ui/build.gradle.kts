plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.account)
                implementation(projects.domain.file)
                implementation(projects.feature.file.api)
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
                implementation(projects.core.testing)
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}
