plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.integrity.api)
                implementation(projects.core.network.api)
                implementation(projects.domain.integrity)
            }
        }
    }
}
