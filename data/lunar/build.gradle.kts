plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.data.core)
                implementation(projects.core.calendarDatabase.api)
                implementation(projects.core.calendarNetwork.api)
                implementation(projects.domain.lunar)
            }
        }
    }
}
