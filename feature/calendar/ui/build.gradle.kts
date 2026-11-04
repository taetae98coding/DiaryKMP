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
                implementation(projects.domain.contact)
                implementation(projects.domain.holiday)
                implementation(projects.domain.lunar)
                implementation(projects.domain.memo)
                implementation(projects.domain.tag)
                implementation(projects.domain.weather)
                implementation(projects.feature.calendar.api)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.core)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.timetable.api)
                implementation(projects.library.kotlinxDatetime)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        androidHostTest {
            dependencies {
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
