plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.datastore.api)
                implementation(projects.core.geminiNetwork.api)
                implementation(projects.core.mapper)
                implementation(projects.domain.setting)
            }
        }
    }
}
