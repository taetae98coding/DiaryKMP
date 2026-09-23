plugins {
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    jvm()

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
