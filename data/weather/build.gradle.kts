plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.ipNetwork.api)
                implementation(projects.core.location.api)
                implementation(projects.core.weatherNetwork.api)
                implementation(projects.domain.weather)
            }
        }
    }
}
