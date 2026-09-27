plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.lifecycle.viewmodel.compose)
                implementation(projects.compose.calendar)
                implementation(projects.domain.holiday)
                implementation(projects.feature.holiday.api)
                implementation(projects.feature.memo.api)
                implementation(projects.library.kotlinxDatetime)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
            }
        }
    }
}
