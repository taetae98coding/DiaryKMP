plugins {
    alias(libs.plugins.convention.work)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.browser)
                implementation(projects.library.coroutines)
            }
        }
    }
}
