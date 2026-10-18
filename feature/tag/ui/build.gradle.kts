plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.list)
                implementation(projects.compose.memo)
                implementation(projects.compose.place)
                implementation(projects.compose.tag)
                implementation(projects.compose.web)
                implementation(projects.domain.location)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.setting)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(projects.feature.core)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.place.api)
                implementation(projects.feature.search.api)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.web.api)
                implementation(projects.library.composeUi)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
                implementation(projects.core.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
