plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.core.mapper)
                implementation(projects.core.youtubeNetwork.api)
                implementation(projects.domain.playlist)
            }
        }
    }
}
