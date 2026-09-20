plugins {
    alias(libs.plugins.primitive.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)

                api(libs.kotlinx.io.core)
            }
        }
    }
}
