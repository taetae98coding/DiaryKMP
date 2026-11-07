plugins {
    alias(libs.plugins.primitive.kmp)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.database.api)
                api(projects.core.googleNetwork.api)
                api(projects.core.ipNetwork.api)
                api(projects.core.location.api)
                api(projects.core.model)
                api(projects.core.naverNetwork.api)
                api(projects.core.network.api)
                api(projects.core.weatherNetwork.api)
                api(projects.library.fixture)
            }
        }
    }
}
