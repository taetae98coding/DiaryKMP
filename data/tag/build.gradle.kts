plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.data.core)
                implementation(projects.domain.tag)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
