plugins {
    alias(libs.plugins.primitive.multiplatform)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.core.datastore.api)
                implementation(projects.core.geminiNetwork.api)
                implementation(projects.core.googleNetwork.api)
                implementation(projects.core.holidayDatabase.api)
                implementation(projects.core.holidayNetwork.api)
                implementation(projects.core.model)
                implementation(projects.core.naverNetwork.api)
                implementation(projects.core.network.api)
                implementation(projects.core.weatherNetwork.api)
                implementation(projects.core.webNetwork.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
