plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.datastore.api)
                implementation(projects.core.holidayDatabase.api)
                implementation(projects.core.holidayNetwork.api)
                implementation(projects.core.mapper)
                implementation(projects.domain.holiday)
            }
        }
    }
}
