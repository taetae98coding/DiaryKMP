plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.checklist)
                implementation(projects.compose.contact)
                implementation(projects.compose.dday)
                implementation(projects.compose.list)
                implementation(projects.compose.memo)
                implementation(projects.compose.music)
                implementation(projects.compose.place)
                implementation(projects.compose.qr)
                implementation(projects.compose.tag)
                implementation(projects.compose.web)
                implementation(projects.domain.checklist)
                implementation(projects.domain.contact)
                implementation(projects.domain.dday)
                implementation(projects.domain.location)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.playlist)
                implementation(projects.domain.qr)
                implementation(projects.domain.setting)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(projects.feature.checklist.api)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.dday.api)
                implementation(projects.feature.core)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.place.api)
                implementation(projects.feature.playlist.api)
                implementation(projects.feature.qr.api)
                implementation(projects.feature.search.api)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.web.api)
                implementation(projects.library.composeUi)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
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
