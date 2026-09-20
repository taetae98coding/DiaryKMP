plugins {
    alias(libs.plugins.primitive.jvm)
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.database.api)
                api(projects.core.model)
                api(projects.core.network.api)
                api(projects.library.fixtureMonkey)
            }
        }
    }
}
