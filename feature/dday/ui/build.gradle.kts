plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.dday)
                implementation(projects.compose.list)
                implementation(projects.compose.tag)
                implementation(projects.domain.dday)
                implementation(projects.domain.tag)
                implementation(projects.feature.core)
                implementation(projects.feature.dday.api)
                implementation(projects.feature.more.api)
                implementation(projects.feature.tag.api)
                implementation(projects.library.composeUi)
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
                implementation(libs.androidx.paging.testing)
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            }
        }
    }
}
