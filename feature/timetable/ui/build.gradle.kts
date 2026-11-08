plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.calendar)
                implementation(projects.compose.contact)
                implementation(projects.compose.timetable)
                implementation(projects.domain.contact)
                implementation(projects.domain.dday)
                implementation(projects.domain.holiday)
                implementation(projects.domain.lunar)
                implementation(projects.domain.memo)
                implementation(projects.domain.weather)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.dday.api)
                implementation(projects.feature.core)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.timetable.api)
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
                implementation(projects.feature.calendar.api)
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            }
        }
    }
}
