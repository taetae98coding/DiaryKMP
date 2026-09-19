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
                implementation(projects.domain.memo)
                implementation(projects.domain.sync)
                implementation(projects.domain.tag)
                implementation(projects.domain.weather)
                implementation(projects.feature.calendar.api)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.memo.api)
                implementation(projects.library.kotlin)
                implementation(projects.library.kotlinxDatetime)
                implementation(libs.coil.compose)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
