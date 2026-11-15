plugins {
    alias(libs.plugins.convention.core.network)
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
