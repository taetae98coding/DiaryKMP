plugins {
    alias(libs.plugins.primitive.kmp)
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
