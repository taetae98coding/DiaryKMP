plugins {
    alias(libs.plugins.convention.core.network)
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(projects.core.dataTesting)
                implementation(projects.core.testing)
            }
        }
    }
}
