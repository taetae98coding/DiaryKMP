plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.feature.core)
                implementation(projects.feature.routine.api)
            }
        }
    }
}
