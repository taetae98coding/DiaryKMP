plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.file.api)

                implementation(projects.domain.authentication)
            }
        }
    }
}
