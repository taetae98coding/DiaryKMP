plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.contact)
                implementation(projects.compose.list)
                implementation(projects.compose.memo)
                implementation(projects.compose.tag)
                implementation(projects.domain.contact)
                implementation(projects.domain.memo)
                implementation(projects.domain.tag)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.core)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.tag.api)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        androidHostTest {
            dependencies {
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
