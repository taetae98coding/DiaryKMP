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
                implementation(projects.compose.place)
                implementation(projects.compose.tag)
                implementation(projects.domain.location)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.setting)
                implementation(projects.domain.sync)
                implementation(projects.domain.tag)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.place.api)
                implementation(projects.feature.search.api)
                implementation(projects.feature.tag.api)
                implementation(projects.library.composeUi)
                implementation(projects.library.kotlin)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
                implementation(libs.androidx.paging.testing)
                implementation(projects.core.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
                implementation(projects.core.testing)
            }
        }
    }
}
