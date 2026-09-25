plugins {
    alias(libs.plugins.primitive.kmp)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.logger.core)
                api(libs.kotlinx.serialization.json)
            }
        }
    }
}
