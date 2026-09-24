plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.datastore.api)
                implementation(projects.core.calendarDatabase.api)
                implementation(projects.core.calendarNetwork.api)
                implementation(projects.domain.holiday)
                implementation(projects.library.locale)
            }
        }
    }
}
