plugins {
    alias(libs.plugins.primitive.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.logger.core)
            }
        }
    }
}
