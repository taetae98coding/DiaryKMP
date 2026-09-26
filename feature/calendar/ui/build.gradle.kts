plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.calendar)
                implementation(projects.compose.permission)
                implementation(projects.compose.tag)
                implementation(projects.compose.timetable)
                implementation(projects.domain.contact)
                implementation(projects.domain.holiday)
                implementation(projects.domain.lunar)
                implementation(projects.domain.memo)
                implementation(projects.domain.sync)
                implementation(projects.domain.tag)
                implementation(projects.domain.weather)
                implementation(projects.feature.calendar.api)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.tag.api)
                implementation(projects.library.composeUi)
                implementation(projects.library.kotlin)
                implementation(projects.library.kotlinxDatetime)
                implementation(libs.coil.compose)
            }
        }

        androidHostTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.androidx.lifecycle.runtime.testing)
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
