plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.list)
                implementation(libs.jetbrains.lifecycle.viewmodel.compose)
                implementation(projects.compose.memo)
                implementation(projects.domain.contact)
                implementation(projects.domain.memo)
                implementation(projects.domain.sync)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.memo.api)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
                implementation(libs.androidx.paging.testing)
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
