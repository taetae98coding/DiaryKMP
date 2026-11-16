plugins {
    alias(libs.plugins.primitive.kmp)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
                api(projects.library.fixture)
            }
        }
    }
}
