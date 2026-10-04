plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.calendar)
                implementation(projects.domain.holiday)
                implementation(projects.feature.holiday.api)
                implementation(projects.feature.memo.api)
                implementation(projects.library.kotlinxDatetime)
            }
        }
    }
}
